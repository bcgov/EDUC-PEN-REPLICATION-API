package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.IndependentSchoolSystem;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.AuthorityCreateService;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.saga.AuthorityCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.struct.saga.AuthorityUpdateSagaData;
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
public class AuthorityUpdateOrchestrator extends BaseOrchestrator<AuthorityUpdateSagaData> {

    public static final String RESPONDED_VIA_NATS_TO_FOR_EVENT = "responded via NATS to {} for {} Event. :: {}";
    private final RestUtils restUtils;

    private AuthorityMasterRepository authorityMasterRepository = null;
    private AuthorityMapperHelper authorityMapperHelper = null;
    private static final AuthorityMapper authorityMapper = AuthorityMapper.mapper;


    protected AuthorityUpdateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final RestUtils restUtils, AuthorityCreateService authorityCreateService) {
        super(entityManagerFactory, sagaService, messagePublisher, AuthorityUpdateSagaData.class, SagaEnum.PEN_REPLICATION_AUTHORITY_UPDATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_AUTHORITY_CREATE_SAGA_TOPIC);
        this.restUtils = restUtils;
        this.authorityMasterRepository = authorityMasterRepository;
        this.authorityMapperHelper = authorityMapperHelper;
    }

    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
                .begin(UPDATE_AUTHORITY_IN_SPM, this::updateAuthorityInSPM)
                .step(UPDATE_AUTHORITY_IN_SPM, AUTHORITY_UPDATED_IN_SPM, UPDATE_AUTHORITY_IN_IOSAS, this::updateAuthorityInIOSAS)
                .step(UPDATE_AUTHORITY_IN_SPM, AUTHORITY_WRITE_SKIPPED_IN_SPM_FOR_DATES, UPDATE_AUTHORITY_IN_IOSAS, this::updateAuthorityInIOSAS)
                .step(UPDATE_AUTHORITY_IN_IOSAS, AUTHORITY_UPDATED_IN_IOSAS, UPDATE_AUTHORITY_IN_ISFS, this::updateAuthorityInISFS)
                .end(UPDATE_AUTHORITY_IN_ISFS, AUTHORITY_UPDATED_IN_ISFS);
    }

    private void updateAuthorityInSPM(final Event event, final Saga saga, final AuthorityUpdateSagaData authorityUpdateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_AUTHORITY_IN_SPM.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        var authority = authorityUpdateSagaData.getIndependentAuthority();
        AuthorityMasterEntity newAuthorityMaster = null;
        Event nextEvent = null;

        val existingSchoolMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());
        if (existingSchoolMasterRecord.isPresent()) {
            val existingAuthorityMaster = existingSchoolMasterRecord.get();
             newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, false);
            authorityMapper.updateAuthorityMaster(newAuthorityMaster, existingAuthorityMaster);
            log.info("Processing choreography update event with payload : {}", newAuthorityMaster);
            authorityMasterRepository.save(existingAuthorityMaster);
        }
        if(newAuthorityMaster != null) {
            nextEvent = Event.builder().sagaId(saga.getSagaId())
                    .eventType(UPDATE_AUTHORITY_IN_SPM)
                    .eventOutcome(AUTHORITY_UPDATED_IN_SPM)
                    .eventPayload(JsonUtil.getJsonStringFromObject(newAuthorityMaster))
                    .build();
        }else{
            nextEvent = Event.builder().sagaId(saga.getSagaId())
                    .eventType(UPDATE_AUTHORITY_IN_SPM)
                    .eventOutcome(AUTHORITY_WRITE_SKIPPED_IN_SPM_FOR_DATES)
                    .eventPayload("Authority not written to SPM due to date")
                    .build();
        }
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_AUTHORITY_IN_SPM, saga.getSagaId());
    }

    private void updateAuthorityInIOSAS(final Event event, final Saga saga, final AuthorityUpdateSagaData authorityUpdateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_AUTHORITY_IN_IOSAS.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        restUtils.createOrUpdateAuthorityInIndependentSchoolSystem(authorityUpdateSagaData.getIndependentAuthority(), IndependentSchoolSystem.IOSAS);

        val nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(UPDATE_AUTHORITY_IN_IOSAS)
                .eventOutcome(AUTHORITY_UPDATED_IN_IOSAS)
                .eventPayload(JsonUtil.getJsonStringFromObject(authorityUpdateSagaData.getIndependentAuthority()))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_AUTHORITY_IN_IOSAS, saga.getSagaId());
    }

    private void updateAuthorityInISFS(final Event event, final Saga saga, final AuthorityUpdateSagaData authorityUpdateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_AUTHORITY_IN_ISFS.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        restUtils.createOrUpdateAuthorityInIndependentSchoolSystem(authorityUpdateSagaData.getIndependentAuthority(), IndependentSchoolSystem.ISFS);

        val nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(UPDATE_AUTHORITY_IN_ISFS)
                .eventOutcome(AUTHORITY_UPDATED_IN_ISFS)
                .eventPayload(JsonUtil.getJsonStringFromObject(authorityUpdateSagaData.getIndependentAuthority()))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_AUTHORITY_IN_ISFS, saga.getSagaId());
    }

}
