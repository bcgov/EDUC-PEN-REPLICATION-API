package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Saga service.
 */
@Service
@Slf4j
public class SagaService {
  /**
   * The Saga repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Getter(PRIVATE)
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Saga service.
   *
   * @param sagaRepository      the saga repository
   * @param sagaEventRepository the saga event repository
   */
  @Autowired
  public SagaService(final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }


  /**
   * Create saga record saga.
   *
   * @param saga the saga
   * @return the saga
   */
  public Saga createSagaRecord(final Saga saga) {
    return this.getSagaRepository().save(saga);
  }

  /**
   * no need to do a get here as it is an attached entity
   * first find the child record, if exist do not add. this scenario may occur in replay process,
   * so dont remove this check. removing this check will lead to duplicate records in the child table.
   *
   * @param saga      the saga object.
   * @param sagaEvent the saga event
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedSagaWithEvents(final Saga saga, final SagaEvent sagaEvent) {
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaRepository().save(saga);
    val result = this.getSagaEventRepository()
      .findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(saga, sagaEvent.getSagaEventOutcome(), sagaEvent.getSagaEventState(), sagaEvent.getSagaStepNumber() - 1); //check if the previous step was same and had same outcome, and it is due to replay.
    if (result.isEmpty()) {
      this.getSagaEventRepository().save(sagaEvent);
    }
  }

  /**
   * Find saga by id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  public Optional<Saga> findSagaById(final UUID sagaId) {
    return this.getSagaRepository().findById(sagaId);
  }

  /**
   * Find all saga states list.
   *
   * @param saga the saga
   * @return the list
   */
  public List<SagaEvent> findAllSagaStates(final Saga saga) {
    return this.getSagaEventRepository().findBySaga(saga);
  }


  /**
   * Update saga record.
   *
   * @param saga the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateSagaRecord(final Saga saga) { // saga here MUST be an attached entity
    this.getSagaRepository().save(saga);
  }

  /**
   * Update attached entity during saga process.
   *
   * @param saga the saga
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedEntityDuringSagaProcess(final Saga saga) {
    this.getSagaRepository().save(saga);
  }


  /**
   * Create saga record in db saga.
   *
   * @param sagaName the saga name
   * @param userName the user name
   * @param payload  the payload
   * @return the saga
   */
  public Saga createSagaRecordInDB(final String sagaName, final String userName, final String payload) {
    final var saga = Saga
      .builder()
      .payload(payload)
      .sagaName(sagaName)
      .status(SagaStatusEnum.STARTED.toString())
      .sagaState(EventType.INITIATED.toString())
      .createDate(LocalDateTime.now())
      .createUser(userName)
      .updateUser(userName)
      .updateDate(LocalDateTime.now())
      .build();
    return this.createSagaRecord(saga);
  }


  /**
   * Find all completable future.
   *
   * @param specs      the saga specs
   * @param pageNumber the page number
   * @param pageSize   the page size
   * @param sorts      the sorts
   * @return the completable future
   */
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public Page<Saga> findAll(final Specification<Saga> specs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    return this.sagaRepository.findAll(specs, PageRequest.of(pageNumber, pageSize, Sort.by(sorts)));
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public Optional<SagaEvent> getSagaEventById(final UUID sagaEventID) {
    return this.sagaEventRepository.findById(sagaEventID);
  }

  public void updateSagaEventRecord(final SagaEvent sagaEventFromDB) {
    this.sagaEventRepository.save(sagaEventFromDB);
  }

  public void deleteSagaEvent(SagaEvent sagaEventFromDB) {
    this.sagaEventRepository.delete(sagaEventFromDB);
  }

  public long findInProgressDemogSagaCount() {
    List<String> sagaNames = new ArrayList<>();
    List<String> sagaStatuses = new ArrayList<>();
    sagaNames.add(SagaEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA.getCode());
    sagaNames.add(SagaEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA.getCode());
    sagaStatuses.add(SagaStatusEnum.STARTED.toString());
    sagaStatuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    return this.sagaRepository.countSagasBySagaNameInAndStatusIn(sagaNames, sagaStatuses);
  }

  public long findInProgressTwinSagaCount() {
    List<String> sagaNames = new ArrayList<>();
    List<String> sagaStatuses = new ArrayList<>();
    sagaNames.add(SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA.getCode());
    sagaNames.add(SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA.getCode());
    sagaStatuses.add(SagaStatusEnum.STARTED.toString());
    sagaStatuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    return this.sagaRepository.countSagasBySagaNameInAndStatusIn(sagaNames, sagaStatuses);
  }
}
