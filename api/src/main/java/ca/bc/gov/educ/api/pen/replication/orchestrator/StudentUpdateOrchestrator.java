package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.StudentMapper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.PenDemogService;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.STUDENT_API_TOPIC;

/**
 * The type Student update orchestrator.
 */
@Component
@Slf4j
public class StudentUpdateOrchestrator extends BaseOrchestrator<StudentUpdateSagaData> {
  private final PenDemogTransactionRepository penDemogTransactionRepository;
  private final RestUtils restUtils;
  private final PenDemogService penDemogService;

  private final JdbcTemplate jdbcTemplate;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                   the saga service
   * @param messagePublisher              the message publisher
   * @param entityManagerFactory          the entity manager factory
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param restUtils                     the rest utils
   * @param penDemogService               the pen demog service
   * @param jdbcTemplate                  the jdbc template
   */
  protected StudentUpdateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final PenDemogTransactionRepository penDemogTransactionRepository, final RestUtils restUtils, final PenDemogService penDemogService, final JdbcTemplate jdbcTemplate) {
    super(entityManagerFactory, sagaService, messagePublisher, StudentUpdateSagaData.class, SagaEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC);
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.restUtils = restUtils;
    this.penDemogService = penDemogService;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(GET_STUDENT, this::getStudentByPen)
      .step(GET_STUDENT, STUDENT_FOUND, UPDATE_STUDENT, this::updateStudent)
      .step(GET_STUDENT, STUDENT_NOT_FOUND, CREATE_STUDENT, this::createStudent)
      .step(CREATE_STUDENT, STUDENT_CREATED, UPDATE_PEN_DEMOG, this::updatePenDemog)
      .step(CREATE_STUDENT, STUDENT_ALREADY_EXIST, UPDATE_PEN_DEMOG, this::updatePenDemog)
      .step(UPDATE_STUDENT, STUDENT_UPDATED, UPDATE_PEN_DEMOG, this::updatePenDemog)
      .step(UPDATE_PEN_DEMOG, PEN_DEMOG_UPDATED, UPDATE_PEN_DEMOG_TRANSACTION, this::updatePenDemogTransaction)
      .end(UPDATE_PEN_DEMOG_TRANSACTION, PEN_DEMOG_TRANSACTION_UPDATED);
  }

  private void createStudent(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_STUDENT.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_STUDENT)
      .replyTo(this.getTopicToSubscribe().getCode())
      .eventPayload(JsonUtil.getJsonStringFromObject(StudentMapper.mapper.toStudent(studentUpdateSagaData.getPenDemogTransaction())))
      .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.getCode(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for CREATE_STUDENT Event. :: {}", saga.getSagaId());
  }

  private void getStudentByPen(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) {
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(GET_STUDENT.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(GET_STUDENT)
      .replyTo(this.getTopicToSubscribe().getCode())
      .eventPayload(studentUpdateSagaData.getStudentUpdate().getPen())
      .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT Event.");
  }

  private void updatePenDemogTransaction(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) throws JsonProcessingException {
    val penDemogTx = studentUpdateSagaData.getPenDemogTransaction();
    this.updatePenDemogTransaction(event, saga, penDemogTx, this.penDemogTransactionRepository);
  }

  private void updatePenDemog(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) {
    saga.setSagaState(UPDATE_PEN_DEMOG.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    final int rowsUpdated = this.createOrUpdatePenDemog(studentUpdateSagaData);
    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_PEN_DEMOG)
      .eventOutcome(PEN_DEMOG_UPDATED)
      .eventPayload(rowsUpdated + "")
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), UPDATE_PEN_DEMOG, saga.getSagaId());
  }

  // this uses jdbc template for the update part due to issues with underlying vms system. otherwise, it raises ORA-02047: cannot join the distributed transaction in progress
  private int createOrUpdatePenDemog(final StudentUpdateSagaData studentUpdateSagaData) {
    final int rowsUpdated;
    val existingPenDemogRecord = this.penDemogService.findPenDemogByPen(StringUtils.rightPad(studentUpdateSagaData.getStudentUpdate().getPen(), 10));
    if (existingPenDemogRecord.isPresent()) {
      val existingPenDemog = existingPenDemogRecord.get();
      val penDemographicsEntity = PenReplicationHelper.getPenDemogFromStudentUpdate(studentUpdateSagaData.getStudentUpdate(), existingPenDemog, this.restUtils);
      rowsUpdated = PenReplicationHelper.updatePenDemogUsingJDBC(penDemographicsEntity, this.jdbcTemplate);
    } else {
      val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(StudentMapper.mapper.toStudentCreate(studentUpdateSagaData.getStudentUpdate()));
      penDemographicsEntity.setCreateDate(LocalDateTime.now());
      penDemographicsEntity.setUpdateDate(LocalDateTime.now());
      penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
      this.penDemogService.savePenDemog(penDemographicsEntity);
      rowsUpdated = 1;
    }
    log.debug("{} rows were inserted/updated in pen demog", rowsUpdated);
    return rowsUpdated;
  }


  private void updateStudent(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) throws JsonProcessingException {
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.
    val studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
    val studentUpdate = StudentMapper.mapper.toStudent(studentUpdateSagaData.getPenDemogTransaction());
    BeanUtils.copyProperties(studentUpdate, studentDataFromEventResponse, "createDate", "createUser", "studentID", "email", "emailVerified", "trueStudentID");
    studentUpdateSagaData.getStudentUpdate().setStudentID(studentDataFromEventResponse.getStudentID());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_STUDENT)
      .replyTo(this.getTopicToSubscribe().getCode())
      .eventPayload(JsonUtil.getJsonStringFromObject(studentDataFromEventResponse))
      .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for UPDATE_STUDENT Event.");
  }
}
