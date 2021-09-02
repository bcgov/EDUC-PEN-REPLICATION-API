package ca.bc.gov.educ.api.pen.replication.processor;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.mappers.StudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.service.PenDemogTransactionService;
import ca.bc.gov.educ.api.pen.replication.service.PenTwinTransactionService;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.saga.PossibleMatchSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import ca.bc.gov.educ.api.pen.replication.validator.PenDemogTransactionValidator;
import ca.bc.gov.educ.api.pen.replication.validator.PenTwinTransactionValidator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.replication.constants.SagaEnum.*;
import static ca.bc.gov.educ.api.pen.replication.constants.TransactionType.*;

/**
 * The type Transaction table records processor.
 */
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
  private final SagaService sagaService;

  /**
   * The Min parallel saga.
   */
  @Value("${max.parallel.saga}")
  Integer maxParallelSaga;

  /**
   * Instantiates a new Transaction table records processor.
   *
   * @param penTwinTransactionRepository  the pen twin transaction repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param orchestrators                 the orchestrators
   * @param stringRedisTemplate           the string redis template
   * @param penDemogTransactionService    the pen demog transaction service
   * @param penTwinTransactionService     the pen twin transaction service
   * @param sagaService                   the saga service
   */
  public TransactionTableRecordsProcessor(final PenTwinTransactionRepository penTwinTransactionRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final List<Orchestrator> orchestrators, final StringRedisTemplate stringRedisTemplate, final PenDemogTransactionService penDemogTransactionService, final PenTwinTransactionService penTwinTransactionService, SagaService sagaService) {
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.stringRedisTemplate = stringRedisTemplate;
    this.penDemogTransactionService = penDemogTransactionService;
    this.penTwinTransactionService = penTwinTransactionService;
    this.sagaService = sagaService;
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
      this.stringRedisTemplate.opsForValue().set(redisKey, "true", 2, TimeUnit.SECONDS); // add timeout of 2 seconds so that it self-expires in case delete operation was not successful.
      List<PenDemogTransaction> penDemogTransactions = null;
      List<PenTwinTransaction> penTwinTransactions = null;
      long inProgressDemogSagaCount = this.sagaService.findInProgressDemogSagaCount();
      long inProgressTwinSagaCount = this.sagaService.findInProgressTwinSagaCount();
      if (inProgressDemogSagaCount < maxParallelSaga) {
        penDemogTransactions = this.penDemogTransactionRepository.findFirst10ByTransactionStatusAndTransactionTypeInOrderByTransactionInsertDateTime(TransactionStatus.PENDING.getCode(), Arrays.asList(CREATE_STUDENT.getCode(), UPDATE_STUDENT.getCode()));
      }
      if (inProgressTwinSagaCount < maxParallelSaga) {
        penTwinTransactions = this.penTwinTransactionRepository.findFirst10ByTransactionStatusAndTransactionTypeInOrderByTransactionInsertDateTime(TransactionStatus.PENDING.getCode(), Arrays.asList(CREATE_TWINS.getCode(), DELETE_TWINS.getCode()));
      }

      if (penTwinTransactions != null && !penTwinTransactions.isEmpty()) {
        this.processTwinTransactions(penTwinTransactions);
      }
      if (penDemogTransactions != null && !penDemogTransactions.isEmpty()) {
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
        val error = PenTwinTransactionValidator.validatePenTwinTransaction(penTwinTransaction);
        if (error.isPresent()) {
          log.error(error.get());
          this.updatePenTwinTransactionToErrorState(penTwinTransaction);
          return;
        }
        if (CREATE_TWINS.getCode().equals(txType)) {
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA);
          val saga = this.penTwinTransactionService.createSagaAndUpdatePenTwinTransaction(orchestrator.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(possibleMatchSagaData), penTwinTransaction);
          orchestrator.startSaga(saga);
        } else if (DELETE_TWINS.getCode().equals(txType)) {
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA);
          val saga = this.penTwinTransactionService.createSagaAndUpdatePenTwinTransaction(orchestrator.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(possibleMatchSagaData), penTwinTransaction);
          orchestrator.startSaga(saga);
        } else {
          log.error("unknown transaction type :: {} found in table, ignoring transaction id :: {}", txType, penTwinTransaction.getTransactionID());
          this.updatePenTwinTransactionToErrorState(penTwinTransaction);
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
        this.stringRedisTemplate.opsForValue().set(redisKey, "true", 1, TimeUnit.MINUTES); // add timeout of one minute so that it self-expires in case delete operation was not successful.
        val txType = penDemogTransaction.getTransactionType();
        if (CREATE_STUDENT.getCode().equals(txType)) {
          val error = PenDemogTransactionValidator.validatePenDemogForCreate(penDemogTransaction);
          if (error.isPresent()) {
            log.error(error.get());
            this.updatePenDemogTransactionToErrorState(penDemogTransaction);
            return;
          }
          val student = StudentMapper.mapper.toStudentCreate(penDemogTransaction);
          if ("'".equals(student.getLegalFirstName())) {
            student.setLegalFirstName(null); // update to null if it is apostrophe only.
          }
          final StudentCreateSagaData studentCreateSagaData = StudentCreateSagaData.builder()
            .penDemogTransaction(penDemogTransaction)
            .studentCreate(student)
            .build();
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_STUDENT_CREATE_SAGA);
          val saga = this.penDemogTransactionService.createSagaAndUpdatePenDemogTransaction(orchestrator.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(studentCreateSagaData), penDemogTransaction);
          orchestrator.startSaga(saga);
        } else if (UPDATE_STUDENT.getCode().equals(txType)) {
          val error = PenDemogTransactionValidator.validatePenDemogForUpdate(penDemogTransaction);
          if (error.isPresent()) {
            log.error(error.get());
            this.updatePenDemogTransactionToErrorState(penDemogTransaction);
            return;
          }
          val student = StudentMapper.mapper.toStudent(penDemogTransaction);
          if ("'".equals(student.getLegalFirstName())) {
            student.setLegalFirstName(null); // update to null if it is apostrophe only.
          }
          val studentUpdateSagaData = StudentUpdateSagaData.builder().penDemogTransaction(penDemogTransaction).studentUpdate(student).build();
          val orchestrator = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_STUDENT_UPDATE_SAGA);
          val saga = this.penDemogTransactionService.createSagaAndUpdatePenDemogTransaction(orchestrator.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(studentUpdateSagaData), penDemogTransaction);
          orchestrator.startSaga(saga);

        } else {
          log.error("unknown transaction type :: {} found in table, ignoring transaction id :: {}", txType, penDemogTransaction.getTransactionID());
          this.updatePenDemogTransactionToErrorState(penDemogTransaction);
        }
        this.stringRedisTemplate.delete(redisKey);
      } else {
        log.debug(SKIP_RECORD_LOG, redisKey);
      }
    }
  }

  private void updatePenDemogTransactionToErrorState(final PenDemogTransaction penDemogTransaction) {
    penDemogTransaction.setTransactionProcessedDateTime(LocalDateTime.now());
    penDemogTransaction.setTransactionStatus(TransactionStatus.ERROR.getCode());
    this.penDemogTransactionRepository.save(penDemogTransaction);
  }

  private void updatePenTwinTransactionToErrorState(final PenTwinTransaction penTwinTransaction) {
    penTwinTransaction.setTransactionProcessedDateTime(LocalDateTime.now());
    penTwinTransaction.setTransactionStatus(TransactionStatus.ERROR.getCode());
    this.penTwinTransactionRepository.save(penTwinTransaction);
  }
}
