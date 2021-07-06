package ca.bc.gov.educ.api.pen.replication.processor;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.mappers.StudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.service.PenDemogTransactionService;
import ca.bc.gov.educ.api.pen.replication.service.PenTwinTransactionService;
import ca.bc.gov.educ.api.pen.replication.struct.saga.PossibleMatchSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.replication.constants.SagaEnum.*;
import static ca.bc.gov.educ.api.pen.replication.constants.TransactionType.*;

@Service
@Slf4j
public class TransactionTableRecordsProcessor {

  private static final String SKIP_RECORD_LOG = "skipping {} record, as it is already processed or being processed.";
  private final PenTwinTransactionRepository penTwinTransactionRepository;
  private final PenDemogTransactionRepository penDemogTransactionRepository;
  private final Map<SagaEnum, Orchestrator> sagaEnumOrchestratorMap = new EnumMap<>(SagaEnum.class);
  private final StringRedisTemplate stringRedisTemplate;
  private final PenDemogTransactionService penDemogTransactionService;
  private final PenTwinTransactionService penTwinTransactionService;

  public TransactionTableRecordsProcessor(final PenTwinTransactionRepository penTwinTransactionRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final List<Orchestrator> orchestrators, final StringRedisTemplate stringRedisTemplate, final PenDemogTransactionService penDemogTransactionService, final PenTwinTransactionService penTwinTransactionService) {
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.stringRedisTemplate = stringRedisTemplate;
    this.penDemogTransactionService = penDemogTransactionService;
    this.penTwinTransactionService = penTwinTransactionService;
    orchestrators.forEach(el -> this.sagaEnumOrchestratorMap.put(el.getSagaName(), el));
  }

  /**
   * To avoid race condition and multiple processing of same record. REDIS is used.
   */
  @Async("transactionTableRecordProcessor")
  public void processUnprocessedRecords() {
    val redisKey = "pen-replication-api::extractPendingRecordsFromTransactionTables";
    val valueFromRedis = this.stringRedisTemplate.opsForValue().get(redisKey);
    if (StringUtils.isBlank(valueFromRedis)) {
      this.stringRedisTemplate.opsForValue().set(redisKey, "true", 1, TimeUnit.MINUTES); // add timeout of one minute so that it self expires in case delete operation was not successful.
      val penTwinTransactions = this.penTwinTransactionRepository.findAllByTransactionStatusOrderByTransactionInsertDateTime(TransactionStatus.PENDING.getCode());
      val penDemogTransactions = this.penDemogTransactionRepository.findAllByTransactionStatusOrderByTransactionInsertDateTime(TransactionStatus.PENDING.getCode());
      if (!penTwinTransactions.isEmpty()) {
        this.processTwinTransactions(penTwinTransactions);
      }
      if (!penDemogTransactions.isEmpty()) {
        this.processDemogTransactions(penDemogTransactions);
      }
      this.stringRedisTemplate.delete(redisKey);// delete the key from redis after it processed.
    } else {
      log.debug(SKIP_RECORD_LOG, redisKey);
    }
  }

  @SneakyThrows
  private void processTwinTransactions(final List<PenTwinTransaction> penTwinTransactions) {
    for (val penTwinTransaction : penTwinTransactions) {
      val redisKey = "pen-replication-api::processTwinTransactions::" + penTwinTransaction.getTransactionID();
      val valueFromRedis = this.stringRedisTemplate.opsForValue().get(redisKey);
      if (StringUtils.isBlank(valueFromRedis)) {
        this.stringRedisTemplate.opsForValue().set(redisKey, "true", 1, TimeUnit.MINUTES); // add timeout of one minute so that it self expires in case delete operation was not successful.
        val txType = penTwinTransaction.getTransactionType();
        final PossibleMatchSagaData possibleMatchSagaData = PossibleMatchSagaData.builder().penTwinTransaction(penTwinTransaction).build();
        if (CREATE_TWINS.getCode().equals(txType)) {
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA);
          val saga = this.penTwinTransactionService.createSagaAndUpdatePenTwinTransaction(orchestrator.getSagaName().getCode(), penTwinTransaction.getTwinUserID(), JsonUtil.getJsonStringFromObject(possibleMatchSagaData), penTwinTransaction);
          orchestrator.startSaga(saga);
        } else if (DELETE_TWINS.getCode().equals(txType)) {
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA);
          val saga = this.penTwinTransactionService.createSagaAndUpdatePenTwinTransaction(orchestrator.getSagaName().getCode(), penTwinTransaction.getTwinUserID(), JsonUtil.getJsonStringFromObject(possibleMatchSagaData), penTwinTransaction);
          orchestrator.startSaga(saga);
        } else {
          log.warn("unknown transaction type :: {} found in table, ignoring", txType);
        }
        this.stringRedisTemplate.delete(redisKey);// delete the key from redis after it processed.
      } else {
        log.debug(SKIP_RECORD_LOG, redisKey);
      }
    }
  }

  @SneakyThrows
  private void processDemogTransactions(final List<PenDemogTransaction> penDemogTransactions) {
    for (val penDemogTransaction : penDemogTransactions) {
      val redisKey = "pen-replication-api::processDemogTransactions::" + penDemogTransaction.getTransactionID();
      val valueFromRedis = this.stringRedisTemplate.opsForValue().get(redisKey);
      if (StringUtils.isBlank(valueFromRedis)) {
        this.stringRedisTemplate.opsForValue().set(redisKey, "true", 1, TimeUnit.MINUTES); // add timeout of one minute so that it self expires in case delete operation was not successful.
        val txType = penDemogTransaction.getTransactionType();
        if (CREATE_STUDENT.getCode().equals(txType)) {
          final StudentCreateSagaData studentCreateSagaData = StudentCreateSagaData.builder()
            .penDemogTransaction(penDemogTransaction)
            .studentCreate(StudentMapper.mapper.toStudentCreate(penDemogTransaction))
            .build();
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_STUDENT_CREATE_SAGA);
          val saga = this.penDemogTransactionService.createSagaAndUpdatePenDemogTransaction(orchestrator.getSagaName().getCode(), penDemogTransaction.getCreateUser(), JsonUtil.getJsonStringFromObject(studentCreateSagaData), penDemogTransaction);
          orchestrator.startSaga(saga);
        } else if (UPDATE_STUDENT.getCode().equals(txType)) {
          val studentUpdateSagaData = StudentUpdateSagaData.builder().penDemogTransaction(penDemogTransaction).build();
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_STUDENT_UPDATE_SAGA);
          val saga = this.penDemogTransactionService.createSagaAndUpdatePenDemogTransaction(orchestrator.getSagaName().getCode(), penDemogTransaction.getCreateUser(), JsonUtil.getJsonStringFromObject(studentUpdateSagaData), penDemogTransaction);
          orchestrator.startSaga(saga);
        } else {
          log.warn("unknown transaction type :: {} found in table, ignoring", txType);
        }
      } else {
        log.debug(SKIP_RECORD_LOG, redisKey);
      }
    }
  }
}
