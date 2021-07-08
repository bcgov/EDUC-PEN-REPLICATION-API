package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinsEntityID;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinsRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.saga.PossibleMatchSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.PEN_MATCH_API_TOPIC;

/**
 * The type Student create orchestrator.
 */
@Component
@Slf4j
public class DeletePossibleMatchOrchestrator extends BaseOrchestrator<PossibleMatchSagaData> {

  private final PenTwinTransactionRepository penTwinTransactionRepository;
  private final RestUtils restUtils;
  private final PenTwinsRepository penTwinsRepository;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                  the saga service
   * @param messagePublisher             the message publisher
   * @param entityManagerFactory         the entity manager factory
   * @param restUtils                    the rest utils
   * @param penTwinTransactionRepository the pen twin transaction repository
   * @param penTwinsRepository           the pen twins repository
   */
  protected DeletePossibleMatchOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final RestUtils restUtils, final PenTwinTransactionRepository penTwinTransactionRepository, final PenTwinsRepository penTwinsRepository) {
    super(entityManagerFactory, sagaService, messagePublisher, PossibleMatchSagaData.class, SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA, SagaTopicsEnum.PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA_TOPIC);
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.restUtils = restUtils;
    this.penTwinsRepository = penTwinsRepository;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(DELETE_POSSIBLE_MATCH, this::deletePossibleMatchFromStudent)
      .step(DELETE_POSSIBLE_MATCH, STUDENTS_NOT_FOUND, DELETE_PEN_TWINS, this::deletePenTwins)
      .step(DELETE_POSSIBLE_MATCH, POSSIBLE_MATCH_DELETED, DELETE_PEN_TWINS, this::deletePenTwins)
      .step(DELETE_PEN_TWINS, PEN_TWINS_DELETED, UPDATE_PEN_TWIN_TRANSACTION, this::updatePenTwinTransaction)
      .end(UPDATE_PEN_TWIN_TRANSACTION, PEN_TWIN_TRANSACTION_UPDATED);
  }

  private void deletePenTwins(final Event event, final Saga saga, final PossibleMatchSagaData possibleMatchSagaData) {
    saga.setSagaState(DELETE_PEN_TWINS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    val penTwinsLeftSideID = PenTwinsEntityID.builder()
      .penTwin1(possibleMatchSagaData.getPenTwinTransaction().getPenTwin1())
      .penTwin2(possibleMatchSagaData.getPenTwinTransaction().getPenTwin2())
      .build();

    val penTwinsRightSideID = PenTwinsEntityID.builder()
      .penTwin1(possibleMatchSagaData.getPenTwinTransaction().getPenTwin2())
      .penTwin2(possibleMatchSagaData.getPenTwinTransaction().getPenTwin1())
      .build();
    val isPenTwinLeftSideExist = this.penTwinsRepository.findById(penTwinsLeftSideID);
    val isPenTwinRightSideExist = this.penTwinsRepository.findById(penTwinsRightSideID);
    var rowsUpdated = 0;
    if (isPenTwinLeftSideExist.isPresent()) {
      val penTwinsDelete = PenReplicationHelper.buildPenTwinDeleteLeftSide(possibleMatchSagaData.getPenTwinTransaction());
      rowsUpdated += this.persistData(penTwinsDelete);
    }
    if (isPenTwinRightSideExist.isPresent()) {
      val penTwinsDelete = PenReplicationHelper.buildPenTwinDeleteRightSide(possibleMatchSagaData.getPenTwinTransaction());
      rowsUpdated += this.persistData(penTwinsDelete);
    }
    val nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(DELETE_PEN_TWINS)
      .eventOutcome(PEN_TWINS_DELETED)
      .eventPayload(rowsUpdated + "")
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), DELETE_PEN_TWINS, saga.getSagaId());
  }

  private void deletePossibleMatchFromStudent(final Event event, final Saga saga, final PossibleMatchSagaData possibleMatchSagaData) throws JsonProcessingException {
    saga.setSagaState(DELETE_POSSIBLE_MATCH.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    final List<String> pens = new ArrayList<>();
    pens.add(possibleMatchSagaData.getPenTwinTransaction().getPenTwin1());
    pens.add(possibleMatchSagaData.getPenTwinTransaction().getPenTwin2());
    val studentMap = this.restUtils.createStudentMapFromPenNumbers(pens, saga.getSagaId());
    if (studentMap.size() != pens.size()) { // size mismatch student does not exist.
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(DELETE_POSSIBLE_MATCH)
        .eventOutcome(STUDENTS_NOT_FOUND)
        .eventPayload("STUDENTS_NOT_FOUND")
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
      log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), DELETE_POSSIBLE_MATCH, saga.getSagaId());
    } else {
      val possibleMatch = new PossibleMatch();
      possibleMatch.setStudentID(studentMap.get(possibleMatchSagaData.getPenTwinTransaction().getPenTwin1()).getStudentID());
      possibleMatch.setMatchedStudentID(studentMap.get(possibleMatchSagaData.getPenTwinTransaction().getPenTwin2()).getStudentID());
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(DELETE_POSSIBLE_MATCH)
        .replyTo(this.getTopicToSubscribe().getCode())
        .eventPayload(JsonUtil.getJsonStringFromObject(Collections.singletonList(possibleMatch)))
        .build();
      this.postMessageToTopic(PEN_MATCH_API_TOPIC.getCode(), nextEvent);
      log.info("message sent to PEN_MATCH_API_TOPIC for DELETE_POSSIBLE_MATCH Event.");
    }
  }

  private void updatePenTwinTransaction(final Event event, final Saga saga, final PossibleMatchSagaData possibleMatchSagaData) throws JsonProcessingException {
    val penTwinTx = possibleMatchSagaData.getPenTwinTransaction();
    this.updatePenTwinTransaction(event, saga, penTwinTx, this.penTwinTransactionRepository);
  }


}
