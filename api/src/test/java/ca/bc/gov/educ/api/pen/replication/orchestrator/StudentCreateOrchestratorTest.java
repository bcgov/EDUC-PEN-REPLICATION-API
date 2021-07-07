package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.*;
import ca.bc.gov.educ.api.pen.replication.mappers.StudentMapper;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCreateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.PEN_SERVICES_API_TOPIC;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.STUDENT_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class StudentCreateOrchestratorTest extends BasePenReplicationAPITest {
  private final String transactionID = "1234567890";
  private final String pen = "120164447";
  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;
  @Autowired
  private SagaService sagaService;
  @Autowired
  private StudentCreateOrchestrator orchestrator;
  @Autowired
  private MessagePublisher messagePublisher;
  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private StudentCreateSagaData sagaData;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    this.sagaData = StudentCreateSagaData.builder().penDemogTransaction(this.createMockPenDemogTransaction()).studentCreate(StudentMapper.mapper.toStudentCreate(this.createMockPenDemogTransaction())).build();
    this.saga = this.sagaService.createSagaRecordInDB(SagaEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA.getCode(), "Test", JsonUtil.objectMapper.writeValueAsString(this.sagaData));
  }


  /**
   * Test get next pen number.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testGetNextPenNumber_givenEventAndSagaData_shouldPostEventToServicesApi() throws IOException, InterruptedException, TimeoutException {
    final var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(PEN_SERVICES_API_TOPIC.getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_NEXT_PEN_NUMBER);
    assertThat(newEvent.getEventPayload()).isEqualTo(this.saga.getSagaId().toString());
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_NEXT_PEN_NUMBER.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testCreateStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var event = Event.builder()
      .eventType(GET_NEXT_PEN_NUMBER)
      .eventOutcome(EventOutcome.NEXT_PEN_NUMBER_RETRIEVED)
      .eventPayload(this.pen)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(STUDENT_API_TOPIC.getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_STUDENT);
    assertThat(newEvent.getEventPayload()).contains("\"pen\":\"120164447\"").contains("\"historyActivityCode\":\"SLD\"");
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_STUDENT.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.GET_NEXT_PEN_NUMBER.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.NEXT_PEN_NUMBER_RETRIEVED.toString());
  }

  @Test
  public void testAddPenDemog_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var event = Event.builder()
      .eventType(CREATE_STUDENT)
      .eventOutcome(EventOutcome.STUDENT_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(this.sagaData.getStudentCreate()))
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(ADD_PEN_DEMOG);
    assertThat(newEvent.getEventPayload()).contains("1");
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(ADD_PEN_DEMOG.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(CREATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_CREATED.toString());
    val penDemog = this.penReplicationTestUtils.getPenDemogRepository().findById(this.pen);
    assertThat(penDemog).isPresent();
  }

  @Test
  public void testUpdatePenDemogTransaction_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    this.penReplicationTestUtils.getPenDemogTransactionRepository().save(this.createMockPenDemogTransaction());
    final var event = Event.builder()
      .eventType(ADD_PEN_DEMOG)
      .eventOutcome(EventOutcome.PEN_DEMOG_ADDED)
      .eventPayload(JsonUtil.getJsonStringFromObject(this.sagaData.getStudentCreate()))
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_PEN_DEMOG_TRANSACTION);
    assertThat(newEvent.getEventPayload()).contains("\"transactionStatus\":\"COMP\"").contains("\"transactionID\":\"1234567890\"");
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_PEN_DEMOG_TRANSACTION.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(ADD_PEN_DEMOG.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.PEN_DEMOG_ADDED.toString());
    val penDemog = this.penReplicationTestUtils.getPenDemogTransactionRepository().findById(this.transactionID);
    assertThat(penDemog).isPresent();
    assertThat(penDemog.get().getTransactionStatus()).isEqualTo(TransactionStatus.COMPLETE.getCode());
  }

  @Test
  public void testMarkSagaComplete_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    this.penReplicationTestUtils.getPenDemogTransactionRepository().save(this.createMockPenDemogTransaction());
    final var event = Event.builder()
      .eventType(UPDATE_PEN_DEMOG_TRANSACTION)
      .eventOutcome(EventOutcome.PEN_DEMOG_TRANSACTION_UPDATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(this.sagaData.getPenDemogTransaction()))
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.SAGA_COMPLETED);
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(COMPLETED.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_PEN_DEMOG_TRANSACTION.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.PEN_DEMOG_TRANSACTION_UPDATED.toString());
    val penDemog = this.penReplicationTestUtils.getPenDemogTransactionRepository().findById(this.transactionID);
    assertThat(penDemog).isPresent();
  }

  public PenDemogTransaction createMockPenDemogTransaction() {
    return PenDemogTransaction.builder()
      .transactionInsertDateTime(LocalDateTime.now())
      .transactionStatus(TransactionStatus.IN_PROGRESS.getCode())
      .transactionType(TransactionType.CREATE_STUDENT.getCode())
      .createUser("test")
      .demogCode("A")
      .birthDate("19800101")
      .givenName("givenName")
      .transactionID(this.transactionID)
      .pen(this.pen)
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
  }
}
