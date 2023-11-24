package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.saga.AuthorityCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;

@Component
@Slf4j
public class AuthorityCreateOrchestrator extends BaseOrchestrator<AuthorityCreateSagaData> {

  private final RestUtils restUtils;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  private AuthorityMasterRepository authorityMasterRepository;
  private final AuthorityMapperHelper authorityMapperHelper;

  protected AuthorityCreateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final RestUtils restUtils, AuthorityMasterRepository authorityMasterRepository, AuthorityMapperHelper authorityMapperHelper) {
    super(entityManagerFactory, sagaService, messagePublisher, AuthorityCreateSagaData.class, SagaEnum.PEN_REPLICATION_AUTHORITY_CREATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_AUTHORITY_CREATE_SAGA_TOPIC);
    this.restUtils = restUtils;
    this.authorityMasterRepository = authorityMasterRepository;
    this.authorityMapperHelper = authorityMapperHelper;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_AUTHORITY_IN_SPM, this::createAuthorityInSPM)
      .step(CREATE_AUTHORITY_IN_SPM, AUTHORITY_CREATED_IN_SPM, CREATE_AUTHORITY_IN_IOSAS, this::createAuthorityInIOSAS)
      .step(CREATE_AUTHORITY_IN_IOSAS, AUTHORITY_CREATED_IN_IOSAS, CREATE_AUTHORITY_IN_ISFS, this::createAuthorityInISFS)
      .end(CREATE_AUTHORITY_IN_ISFS, AUTHORITY_CREATED_IN_ISFS);
  }

  private void createAuthorityInSPM(final Event event, final Saga saga, final AuthorityCreateSagaData authorityCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_AUTHORITY_IN_SPM.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var authority = authorityCreateSagaData.getIndependentAuthority();
    AuthorityMasterEntity newAuthorityMaster = null;

    if (StringUtils.isNotEmpty(authority.getOpenedDate()) && dateTimeMapper.map(authority.getOpenedDate()).isBefore(LocalDateTime.now())){
      val existingSchoolMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());
      if (!existingSchoolMasterRecord.isPresent()) {
        newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, true);
        authorityMasterRepository.save(newAuthorityMaster);
      }
    }

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_AUTHORITY_IN_SPM)
            .eventOutcome(AUTHORITY_CREATED_IN_SPM)
            .eventPayload(JsonUtil.getJsonStringFromObject(newAuthorityMaster))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), CREATE_AUTHORITY_IN_SPM, saga.getSagaId());
  }

  private void createAuthorityInIOSAS(final Event event, final Saga saga, final AuthorityCreateSagaData authorityCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_AUTHORITY_IN_IOSAS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    AuthorityMasterEntity newAuthorityMaster = null;

    //Do something here

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_AUTHORITY_IN_IOSAS)
            .eventOutcome(AUTHORITY_CREATED_IN_IOSAS)
            .eventPayload(JsonUtil.getJsonStringFromObject(newAuthorityMaster))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), CREATE_AUTHORITY_IN_IOSAS, saga.getSagaId());
  }

  private void createAuthorityInISFS(final Event event, final Saga saga, final AuthorityCreateSagaData authorityCreateSagaData) throws JsonProcessingException {
    saga.setSagaState(CREATE_AUTHORITY_IN_ISFS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    AuthorityMasterEntity newAuthorityMaster = null;

    //Do something here

    val nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_AUTHORITY_IN_ISFS)
            .eventOutcome(AUTHORITY_CREATED_IN_ISFS)
            .eventPayload(JsonUtil.getJsonStringFromObject(newAuthorityMaster))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
    log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), CREATE_AUTHORITY_IN_ISFS, saga.getSagaId());
  }

}
