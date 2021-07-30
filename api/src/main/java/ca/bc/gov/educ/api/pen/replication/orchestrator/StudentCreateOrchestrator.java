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
import ca.bc.gov.educ.api.pen.replication.service.PenDemogTransactionService;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.PEN_SERVICES_API_TOPIC;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.STUDENT_API_TOPIC;

/**
 * The type Student create orchestrator.
 */
@Component
@Slf4j
public class StudentCreateOrchestrator extends BaseOrchestrator<StudentCreateSagaData> {

  private final PenDemogTransactionRepository penDemogTransactionRepository;
  private final RestUtils restUtils;
  private final PenDemogService penDemogService;
  private final PenDemogTransactionService penDemogTransactionService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                   the saga service
   * @param messagePublisher              the message publisher
   * @param entityManagerFactory          the entity manager factory
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param restUtils                     the rest utils
   * @param penDemogService               the pen demog service
   * @param penDemogTransactionService    the pen demog transaction service
   */
  protected StudentCreateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final PenDemogTransactionRepository penDemogTransactionRepository, final RestUtils restUtils, final PenDemogService penDemogService, final PenDemogTransactionService penDemogTransactionService) {
    super(entityManagerFactory, sagaService, messagePublisher, StudentCreateSagaData.class, SagaEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC);
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.restUtils = restUtils;
    this.penDemogService = penDemogService;
    this.penDemogTransactionService = penDemogTransactionService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(GET_NEXT_PEN_NUMBER, this::getNextPenNumber)
      .step(GET_NEXT_PEN_NUMBER, NEXT_PEN_NUMBER_RETRIEVED, CREATE_STUDENT, this::createStudent)
      .step(CREATE_STUDENT, STUDENT_ALREADY_EXIST, ADD_PEN_DEMOG, this::addPenDemog)
      .step(CREATE_STUDENT, STUDENT_CREATED, ADD_PEN_DEMOG, this::addPenDemog)
      .step(ADD_PEN_DEMOG, PEN_DEMOG_ADDED, UPDATE_PEN_DEMOG_TRANSACTION, this::updatePenDemogTransaction)
      .end(UPDATE_PEN_DEMOG_TRANSACTION, PEN_DEMOG_TRANSACTION_UPDATED);
  }

  private void updatePenDemogTransaction(final Event event, final Saga saga, final StudentCreateSagaData studentCreateSagaData) throws JsonProcessingException {
    val penDemogTx = studentCreateSagaData.getPenDemogTransaction();
    this.updatePenDemogTransaction(event, saga, penDemogTx, this.penDemogTransactionRepository);
  }


  private void addPenDemog(final Event event, final Saga saga, final StudentCreateSagaData studentCreateSagaData) {
    saga.setSagaState(ADD_PEN_DEMOG.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    final int rowsUpdated = this.createOrUpdatePenDemog(studentCreateSagaData);
    log.debug("{} rows were inserted to pen demog", rowsUpdated);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(ADD_PEN_DEMOG)
      .eventOutcome(PEN_DEMOG_ADDED)
      .eventPayload(rowsUpdated + "")
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), ADD_PEN_DEMOG, saga.getSagaId());
  }

  private int createOrUpdatePenDemog(final StudentCreateSagaData studentCreateSagaData) {
    final int rowsUpdated;
    val existingPenDemogRecord = this.penDemogService.findPenDemogByPen(StringUtils.rightPad(studentCreateSagaData.getStudentCreate().getPen(), 10));
    if (existingPenDemogRecord.isPresent()) {
      val existingPenDemog = existingPenDemogRecord.get();
      val penDemographicsEntity = PenReplicationHelper.getPenDemogFromStudentUpdate(StudentMapper.mapper.toStudentUpdate(studentCreateSagaData.getStudentCreate()), existingPenDemog, this.restUtils);
      BeanUtils.copyProperties(penDemographicsEntity, existingPenDemog, "createDate", "createUser");
      BeanUtils.copyProperties(penDemographicsEntity, existingPenDemog, "createDate", "createUser", "studNo");
      this.penDemogService.savePenDemog(existingPenDemog);
      rowsUpdated = 1;
    } else {
      val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentCreateSagaData.getStudentCreate());
      penDemographicsEntity.setCreateDate(LocalDateTime.now());
      penDemographicsEntity.setUpdateDate(LocalDateTime.now());
      penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
      this.penDemogService.savePenDemog(penDemographicsEntity);
      rowsUpdated = 1;
    }
    log.debug("{} rows were inserted/updated in pen demog", rowsUpdated);
    return rowsUpdated;
  }

  private void createStudent(final Event event, final Saga saga, final StudentCreateSagaData studentCreateSagaData) throws JsonProcessingException {
    final var pen = event.getEventPayload();
    final var student = studentCreateSagaData.getStudentCreate();
    student.setPen(pen);
    // update the pen in pen demog transaction first.
    this.penDemogTransactionService.addPenNumberToTransactionTable(studentCreateSagaData.getPenDemogTransaction().getTransactionID(), pen);
    saga.setSagaState(CREATE_STUDENT.toString());
    saga.setPayload(JsonUtil.getJsonStringFromObject(studentCreateSagaData));
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_STUDENT)
      .replyTo(this.getTopicToSubscribe().getCode())
      .eventPayload(JsonUtil.getJsonStringFromObject(student))
      .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.getCode(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for CREATE_STUDENT Event. :: {}", saga.getSagaId());
  }

  private void getNextPenNumber(final Event event, final Saga saga, final StudentCreateSagaData studentCreateSagaData) {
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_NEXT_PEN_NUMBER.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    val transactionID = saga.getSagaId().toString();
    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(GET_NEXT_PEN_NUMBER)
      .replyTo(this.getTopicToSubscribe().getCode())
      .eventPayload(transactionID)
      .build();
    this.postMessageToTopic(PEN_SERVICES_API_TOPIC.getCode(), nextEvent);
    log.info("message sent to PEN_SERVICES_API_TOPIC for GET_NEXT_PEN_NUMBER Event. :: {}", saga.getSagaId());
  }
}
