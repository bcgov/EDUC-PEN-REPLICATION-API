package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.IndependentSchoolSystem;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SchoolCreateService;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SchoolCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;

@Component
@Slf4j
public class SchoolCreateOrchestrator extends BaseOrchestrator<SchoolCreateSagaData> {

  public static final String RESPONDED_VIA_NATS_TO_FOR_EVENT = "responded via NATS to {} for {} Event. :: {}";
  private final RestUtils restUtils;
  private final SchoolCreateService schoolCreateService;

  protected SchoolCreateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final RestUtils restUtils, SchoolCreateService schoolCreateService) {
    super(entityManagerFactory, sagaService, messagePublisher, SchoolCreateSagaData.class, SagaEnum.PEN_REPLICATION_SCHOOL_CREATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_SCHOOL_CREATE_SAGA_TOPIC);
    this.restUtils = restUtils;
    this.schoolCreateService = schoolCreateService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SCHOOL_IN_SPM, this::createSchoolInSPM)
      .step(CREATE_SCHOOL_IN_SPM, SCHOOL_CREATED_IN_SPM, CREATE_SCHOOL_IN_IOSAS, this::createSchoolInIOSAS)
      .step(CREATE_SCHOOL_IN_SPM, SCHOOL_WRITE_SKIPPED_IN_SPM_FOR_DATES, CREATE_SCHOOL_IN_IOSAS, this::createSchoolInIOSAS)
      .step(CREATE_SCHOOL_IN_IOSAS, SCHOOL_CREATED_IN_IOSAS, CREATE_SCHOOL_IN_ISFS, this::createSchoolInISFS)
      .end(CREATE_SCHOOL_IN_ISFS, SCHOOL_CREATED_IN_ISFS);
  }

  private void createSchoolInSPM(final Event event, final Saga saga, final SchoolCreateSagaData schoolCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_SCHOOL_IN_SPM.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var school = schoolCreateSagaData.getSchool();

    SchoolMasterEntity newSchoolMaster = schoolCreateService.saveSchool(school);
    Event nextEvent = null;
    if(newSchoolMaster != null) {
      nextEvent = Event.builder().sagaId(saga.getSagaId())
              .eventType(CREATE_SCHOOL_IN_SPM)
              .eventOutcome(SCHOOL_CREATED_IN_SPM)
              .eventPayload(JsonUtil.getJsonStringFromObject(newSchoolMaster))
              .build();
    }else{
      nextEvent = Event.builder().sagaId(saga.getSagaId())
              .eventType(CREATE_SCHOOL_IN_SPM)
              .eventOutcome(SCHOOL_WRITE_SKIPPED_IN_SPM_FOR_DATES)
              .eventPayload("No update required")
              .build();
    }
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), CREATE_SCHOOL_IN_SPM, saga.getSagaId());
  }

  private void createSchoolInIOSAS(final Event event, final Saga saga, final SchoolCreateSagaData schoolCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_SCHOOL_IN_IOSAS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    restUtils.createOrUpdateSchoolInIndependentSchoolSystem(schoolCreateSagaData.getSchool(), IndependentSchoolSystem.IOSAS);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_SCHOOL_IN_IOSAS)
            .eventOutcome(SCHOOL_CREATED_IN_IOSAS)
            .eventPayload(JsonUtil.getJsonStringFromObject(schoolCreateSagaData.getSchool()))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), CREATE_SCHOOL_IN_IOSAS, saga.getSagaId());
  }

  private void createSchoolInISFS(final Event event, final Saga saga, final SchoolCreateSagaData schoolCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_SCHOOL_IN_ISFS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    restUtils.createOrUpdateSchoolInIndependentSchoolSystem(schoolCreateSagaData.getSchool(), IndependentSchoolSystem.ISFS);

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_SCHOOL_IN_ISFS)
            .eventOutcome(SCHOOL_CREATED_IN_ISFS)
            .eventPayload(JsonUtil.getJsonStringFromObject(schoolCreateSagaData.getSchool()))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), CREATE_SCHOOL_IN_ISFS, saga.getSagaId());
  }

}
