package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.IndependentSchoolSystem;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapperHelper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.SchoolMasterRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.service.SchoolCreateService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SchoolUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import ca.bc.gov.educ.api.pen.replication.util.ReplicationUtils;
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
public class SchoolUpdateOrchestrator extends BaseOrchestrator<SchoolUpdateSagaData> {

    public static final String RESPONDED_VIA_NATS_TO_FOR_EVENT = "responded via NATS to {} for {} Event. :: {}";
    private final RestUtils restUtils;
    private final SchoolCreateService schoolCreateService;
    private final SchoolMasterRepository schoolMasterRepository;
    private final SchoolMapperHelper schoolMapperHelper;

    private final LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

    private static final SchoolMapper schoolMapper = SchoolMapper.mapper;


    protected SchoolUpdateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final EntityManagerFactory entityManagerFactory, final RestUtils restUtils, SchoolCreateService schoolCreateService, SchoolMasterRepository schoolMasterRepository, final SchoolMapperHelper schoolMapperHelper) {
        super(entityManagerFactory, sagaService, messagePublisher, SchoolUpdateSagaData.class, SagaEnum.PEN_REPLICATION_SCHOOL_CREATE_SAGA, SagaTopicsEnum.PEN_REPLICATION_SCHOOL_CREATE_SAGA_TOPIC);
        this.restUtils = restUtils;
        this.schoolCreateService = schoolCreateService;
        this.schoolMasterRepository = schoolMasterRepository;
        this.schoolMapperHelper = schoolMapperHelper;


    }
    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
                .begin(UPDATE_SCHOOL_IN_SPM, this::updateSchoolInSPM)
                .step(UPDATE_SCHOOL_IN_SPM, SCHOOL_UPDATED_IN_SPM, UPDATE_SCHOOL_IN_IOSAS, this::updateSchoolInIOSAS)
                .step(UPDATE_SCHOOL_IN_SPM, SCHOOL_WRITE_SKIPPED_IN_SPM_FOR_DATES, UPDATE_SCHOOL_IN_IOSAS, this::updateSchoolInIOSAS)
                .step(UPDATE_SCHOOL_IN_IOSAS, SCHOOL_UPDATED_IN_IOSAS, UPDATE_SCHOOL_IN_ISFS, this::updateSchoolInISFS)
                .end(UPDATE_SCHOOL_IN_ISFS, SCHOOL_UPDATED_IN_ISFS);
    }

    private void updateSchoolInSPM(final Event event, final Saga saga, final SchoolUpdateSagaData schoolUpdateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_SCHOOL_IN_SPM.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        var school = schoolUpdateSagaData.getSchool();
        SchoolMasterEntity existingSchoolMaster = null;
        Event nextEvent = null;

        if (!shouldIgnoreSchoolRecordDueToDates(school)){
            //This is a never opened school, or future open date scenario, don't process this event
            var mincode = new Mincode();
            mincode.setSchlNo(school.getSchoolNumber());
            mincode.setDistNo(school.getMincode().substring(0,3));
            val existingSchoolMasterRecord = this.schoolMasterRepository.findById(mincode);

            if (existingSchoolMasterRecord.isPresent()) {
                 existingSchoolMaster = existingSchoolMasterRecord.get();
                ReplicationUtils.setCloseDateIfRequired(school, existingSchoolMaster);
                val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, false);
                schoolMapper.updateSchoolMaster(newSchoolMaster, existingSchoolMaster);
                log.info("Processing choreography update event with payload : {}", newSchoolMaster);
                schoolMasterRepository.save(existingSchoolMaster);
            } else {
                ReplicationUtils.setCloseDateIfRequired(school, null);
                // School needs to be created
                val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, true);
                log.info("Processing choreography update event with payload : {}", newSchoolMaster);
                schoolMasterRepository.save(newSchoolMaster);
            }
        }

        if(existingSchoolMaster != null) {
            nextEvent = Event.builder().sagaId(saga.getSagaId())
                    .eventType(UPDATE_SCHOOL_IN_SPM)
                    .eventOutcome(SCHOOL_UPDATED_IN_SPM)
                    .eventPayload(JsonUtil.getJsonStringFromObject(existingSchoolMaster))
                    .build();
        }else{
            nextEvent = Event.builder().sagaId(saga.getSagaId())
                    .eventType(UPDATE_SCHOOL_IN_SPM)
                    .eventOutcome(SCHOOL_WRITE_SKIPPED_IN_SPM_FOR_DATES)
                    .eventPayload("No update required")
                    .build();
        }
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_SCHOOL_IN_SPM, saga.getSagaId());
    }

    private boolean shouldIgnoreSchoolRecordDueToDates(School school){
        return (StringUtils.isEmpty(school.getOpenedDate()) && StringUtils.isEmpty(school.getClosedDate())) || (StringUtils.isNotEmpty(school.getOpenedDate()) && dateTimeMapper.map(school.getOpenedDate()).isAfter(LocalDateTime.now()));
    }
    private void updateSchoolInIOSAS(final Event event, final Saga saga, final SchoolUpdateSagaData schoolCreateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_SCHOOL_IN_IOSAS.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        restUtils.createOrUpdateSchoolInIndependentSchoolSystem(schoolCreateSagaData.getSchool(), IndependentSchoolSystem.IOSAS);

        val nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(UPDATE_SCHOOL_IN_IOSAS)
                .eventOutcome(SCHOOL_UPDATED_IN_IOSAS)
                .eventPayload(JsonUtil.getJsonStringFromObject(schoolCreateSagaData.getSchool()))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_SCHOOL_IN_IOSAS, saga.getSagaId());
    }

    private void updateSchoolInISFS(final Event event, final Saga saga, final SchoolUpdateSagaData schoolCreateSagaData) throws JsonProcessingException {
        saga.setSagaState(UPDATE_SCHOOL_IN_ISFS.toString());
        final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        restUtils.createOrUpdateSchoolInIndependentSchoolSystem(schoolCreateSagaData.getSchool(), IndependentSchoolSystem.ISFS);

        val nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(UPDATE_SCHOOL_IN_ISFS)
                .eventOutcome(SCHOOL_UPDATED_IN_ISFS)
                .eventPayload(JsonUtil.getJsonStringFromObject(schoolCreateSagaData.getSchool()))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
        log.info(RESPONDED_VIA_NATS_TO_FOR_EVENT, this.getTopicToSubscribe(), UPDATE_SCHOOL_IN_ISFS, saga.getSagaId());
    }

}

