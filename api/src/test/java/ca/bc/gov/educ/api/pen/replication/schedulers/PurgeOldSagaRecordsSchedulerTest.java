package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.EventStatus;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

public class PurgeOldSagaRecordsSchedulerTest extends BasePenReplicationAPITest {

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  EventRepository eventRepository;

  @Autowired
  PurgeOldSagaRecordsScheduler purgeOldSagaRecordsScheduler;


  @Test
  public void pollSagaTableAndPurgeOldRecords_givenOldRecordsPresent_shouldBeDeleted() {
    final String penRequestBatchID = "7f000101-7151-1d84-8171-5187006c0000";
    final String getPenRequestBatchStudentID = "7f000101-7151-1d84-8171-5187006c0001";
    final var payload = " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"penRequestBatchID\": \"" + penRequestBatchID + "\",\n" +
        "    \"penRequestBatchStudentID\": \"" + getPenRequestBatchStudentID + "\",\n" +
        "    \"legalFirstName\": \"Jack\"\n" +
        "  }";
    final var saga_today = this.getSaga(payload, LocalDateTime.now());
    final var yesterday = LocalDateTime.now().minusDays(1);
    final var saga_yesterday = this.getSaga(payload, yesterday);

    this.repository.save(saga_today);
    this.sagaEventRepository.save(this.getSagaEvent(saga_today, payload));
    this.eventRepository.save(this.getEvent(saga_today, payload, LocalDateTime.now()));

    this.repository.save(saga_yesterday);
    this.sagaEventRepository.save(this.getSagaEvent(saga_yesterday, payload));
    this.eventRepository.save(this.getEvent(saga_yesterday, payload, yesterday));

    this.purgeOldSagaRecordsScheduler.setSagaRecordStaleInDays(1);
    this.purgeOldSagaRecordsScheduler.purgeOldRecords();
    final var sagas = this.repository.findAll();
    assertThat(sagas).hasSize(1);

    final var sagaEvents = this.sagaEventRepository.findAll();
    assertThat(sagaEvents).hasSize(1);

    final var servicesEvents = this.eventRepository.findAll();
    assertThat(servicesEvents).hasSize(1);
  }


  private Saga getSaga(final String payload, final LocalDateTime createDateTime) {
    return Saga
        .builder()
        .payload(payload)
        .sagaName("PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA")
        .status(COMPLETED.toString())
        .sagaState(COMPLETED.toString())
        .createDate(createDateTime)
        .createUser("PEN_REPLICATION_API")
        .updateUser("PEN_REPLICATION_API")
        .updateDate(createDateTime)
        .build();
  }

  private SagaEvent getSagaEvent(final Saga saga, final String payload) {
    return SagaEvent
        .builder()
        .sagaEventResponse(payload)
        .saga(saga)
        .sagaEventState("ADD_POSSIBLE_MATCH")
        .sagaStepNumber(1)
        .sagaEventOutcome("POSSIBLE_MATCH_ADDED")
        .createDate(LocalDateTime.now())
        .createUser("PEN_REPLICATION_API")
        .updateUser("PEN_REPLICATION_API")
        .updateDate(LocalDateTime.now())
        .build();
  }

  private Event getEvent(final Saga saga, final String payload, final LocalDateTime createDateTime) {
    return Event
      .builder()
      .eventPayloadBytes(payload.getBytes())
      .eventStatus(EventStatus.PROCESSED.toString())
      .eventType("UPDATE_STUDENT")
      .eventOutcome("STUDENT_UPDATED")
      .createDate(createDateTime)
      .createUser("PEN_REPLICATION_API")
      .updateUser("PEN_REPLICATION_API")
      .updateDate(createDateTime)
      .build();
  }
}
