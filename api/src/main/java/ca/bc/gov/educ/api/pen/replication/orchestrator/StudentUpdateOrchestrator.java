package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.StudentMapper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
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
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.UUID;

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


  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                   the saga service
   * @param messagePublisher              the message publisher
   * @param entityManagerFactory          the entity manager factory
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param restUtils                     the rest utils
   * @param penDemogService               the pen demog service
   */
  protected StudentUpdateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final PenDemogTransactionRepository penDemogTransactionRepository, final RestUtils restUtils, final PenDemogService penDemogService) {
    super(entityManagerFactory, sagaService, messagePublisher, StudentUpdateSagaData.class, SagaEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC);
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.restUtils = restUtils;
    this.penDemogService = penDemogService;
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

  private void updatePenDemog(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) throws JsonProcessingException {
    saga.setSagaState(UPDATE_PEN_DEMOG.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    val studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
    final PenDemographicsEntity entity = this.createOrUpdatePenDemog(studentUpdateSagaData, studentDataFromEventResponse, saga.getSagaId());
    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_PEN_DEMOG)
      .eventOutcome(PEN_DEMOG_UPDATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(entity))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), UPDATE_PEN_DEMOG, saga.getSagaId());
  }

  // this uses jdbc template for the update part due to issues with underlying vms system. otherwise, it raises ORA-02047: cannot join the distributed transaction in progress
  private PenDemographicsEntity createOrUpdatePenDemog(final StudentUpdateSagaData studentUpdateSagaData, final StudentUpdate studentDataFromEventResponse, UUID sagaID) {
    val existingPenDemogRecord = this.penDemogService.findPenDemogByPen(studentDataFromEventResponse.getPen());
    if (existingPenDemogRecord.isPresent()) {
      val existingPenDemog = existingPenDemogRecord.get();

      val penDemographicsEntity = PenReplicationHelper.getPenDemogFromStudentUpdate(studentDataFromEventResponse, existingPenDemog, this.restUtils);
      PenDemogStudentMapper.mapper.updatePenDemog(penDemographicsEntity, existingPenDemog);
      log.debug("existing pen demog is :: {}", existingPenDemog);
      if (studentUpdateSagaData.getPenDemogTransaction().getUpdateDemogDate() != null) {
        existingPenDemog.setUpdateDemogDate(studentUpdateSagaData.getPenDemogTransaction().getUpdateDemogDate().toLocalDate());
      }
      if (StringUtils.isNotBlank(studentDataFromEventResponse.getGradeYear()) && StringUtils.isNumeric(studentDataFromEventResponse.getGradeYear())) {
        existingPenDemog.setGradeYear(studentDataFromEventResponse.getGradeYear());
      }
      log.info("Update Orchestrator - Saving PEN Demog update as part of SAGA ID {} :: {}", sagaID.toString(), existingPenDemog);
      return this.penDemogService.savePenDemog(existingPenDemog);
    } else {
      val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(StudentMapper.mapper.toStudentCreate(studentDataFromEventResponse));
      penDemographicsEntity.setCreateDate(LocalDateTime.now());
      penDemographicsEntity.setUpdateDate(LocalDateTime.now());
      penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
      if (studentUpdateSagaData.getPenDemogTransaction().getUpdateDemogDate() != null) {
        penDemographicsEntity.setUpdateDemogDate(studentUpdateSagaData.getPenDemogTransaction().getUpdateDemogDate().toLocalDate());
      }
      if (StringUtils.isNotBlank(studentDataFromEventResponse.getGradeYear()) && StringUtils.isNumeric(studentDataFromEventResponse.getGradeYear())) {
        penDemographicsEntity.setGradeYear(studentDataFromEventResponse.getGradeYear());
      }
      log.info("Update Orchestrator - saving PEN Demog create as part of SAGA ID {} :: {}", sagaID.toString(), penDemographicsEntity);
      return this.penDemogService.savePenDemog(penDemographicsEntity);
    }
  }


  private void updateStudent(final Event event, final Saga saga, final StudentUpdateSagaData studentUpdateSagaData) throws JsonProcessingException {
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.
    val studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
    val studentUpdate = StudentMapper.mapper.toStudent(studentUpdateSagaData.getPenDemogTransaction());

    studentDataFromEventResponse.setUsualFirstName(StringUtils.trimToNull(studentUpdate.getUsualFirstName()));
    studentDataFromEventResponse.setUsualLastName(StringUtils.trimToNull(studentUpdate.getUsualLastName()));
    studentDataFromEventResponse.setUsualMiddleNames(StringUtils.trimToNull(studentUpdate.getUsualMiddleNames()));
    studentDataFromEventResponse.setMincode(StringUtils.trimToNull(studentUpdate.getMincode()));
    studentDataFromEventResponse.setLocalID(StringUtils.trimToNull(studentUpdate.getLocalID()));
    studentDataFromEventResponse.setGradeCode(StringUtils.trimToNull(studentUpdate.getGradeCode()));
    studentDataFromEventResponse.setGradeYear(StringUtils.trimToNull(studentUpdate.getGradeYear()));
    studentDataFromEventResponse.setPostalCode(StringUtils.strip(studentUpdate.getPostalCode(), " "));
    studentDataFromEventResponse.setHistoryActivityCode("SLD");

    String updateUser = StringUtils.trimToNull(studentUpdate.getUpdateUser());
    studentDataFromEventResponse.setUpdateUser(updateUser != null ? updateUser : "REPLICATION_API");

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
