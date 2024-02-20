package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.exception.BusinessException;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import ca.bc.gov.educ.api.pen.replication.struct.saga.AuthorityCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SchoolCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.pen.replication.constants.SagaEnum.PEN_REPLICATION_AUTHORITY_CREATE_SAGA;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaEnum.PEN_REPLICATION_SCHOOL_CREATE_SAGA;


/**
 * This class is responsible to process events from Jet Stream..
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {

  private final ChoreographedEventPersistenceService choreographedEventPersistenceService;
  private final ChoreographEventHandler choreographer;
  private final SagaService sagaService;
  private final Map<SagaEnum, Orchestrator> sagaEnumOrchestratorMap = new EnumMap<>(SagaEnum.class);
  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param choreographedEventPersistenceService the choreographed event persistence service
   * @param choreographer                        the choreographer
   * @param sagaService
   */
  @Autowired
  public EventHandlerDelegatorService(final ChoreographedEventPersistenceService choreographedEventPersistenceService, final List<Orchestrator> orchestrators, final ChoreographEventHandler choreographer, SagaService sagaService) {
    this.choreographedEventPersistenceService = choreographedEventPersistenceService;
    this.choreographer = choreographer;
    this.sagaService = sagaService;
    orchestrators.forEach(el -> this.sagaEnumOrchestratorMap.put(el.getSagaName(), el));
  }

  /**
   * this method will do the following.
   * 1. Call service to store the event in oracle DB.
   * 2. Acknowledge to STAN only when the service call is completed. since it uses manual acknowledgement.
   * 3. Hand off the task to update RDB onto a different executor.
   *
   * @param choreographedEvent the choreographed event
   * @param message            the message
   * @throws IOException the io exception
   */
  public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) throws IOException {
    try {
      switch (choreographedEvent.getEventType()) {
        case CREATE_AUTHORITY:
          log.info("Persisting CREATE_AUTHORITY event record for Saga processing :: {} ", choreographedEvent);
          val orchestratorCreateAuthority = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_AUTHORITY_CREATE_SAGA);
          val authority = JsonUtil.getJsonObjectFromString(IndependentAuthority.class, choreographedEvent.getEventPayload());
          final AuthorityCreateSagaData authorityCreateSagaData = AuthorityCreateSagaData.builder()
                  .independentAuthority(authority)
                  .build();
          val saga = this.sagaService.persistSagaData(orchestratorCreateAuthority.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(authorityCreateSagaData), choreographedEvent.getEventID());
          message.ack();
          log.info("Acknowledged CREATE_AUTHORITY event to Jet Stream...");
          orchestratorCreateAuthority.startSaga(saga);
          break;
        case CREATE_SCHOOL:
          log.info("Persisting CREATE_SCHOOL event record for Saga processing :: {} ", choreographedEvent);
          val orchestratorCreateSchool = this.sagaEnumOrchestratorMap.get(PEN_REPLICATION_SCHOOL_CREATE_SAGA);
          val school = JsonUtil.getJsonObjectFromString(School.class, choreographedEvent.getEventPayload());
          final SchoolCreateSagaData schoolCreateSagaData = SchoolCreateSagaData.builder()
                  .school(school)
                  .build();
          val sagaCreateSchool = this.sagaService.persistSagaData(orchestratorCreateSchool.getSagaName().getCode(), ApplicationProperties.API_NAME, JsonUtil.getJsonStringFromObject(schoolCreateSagaData), choreographedEvent.getEventID());
          message.ack();
          log.info("Acknowledged CREATE_SCHOOL event to Jet Stream...");
          orchestratorCreateSchool.startSaga(sagaCreateSchool);
          break;
        default:
          log.info("Persisting event record for choreography processing :: {} ", choreographedEvent);
          final var persistedEvent = this.choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
          message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
          log.info("Acknowledged to Jet Stream...");
          this.choreographer.handleEvent(persistedEvent);
          break;
      }
    } catch (final BusinessException businessException) {
      message.ack(); // acknowledge to Jet Stream that api got the message already...
      log.info("acknowledged to Jet Stream...");
    }
  }
}
