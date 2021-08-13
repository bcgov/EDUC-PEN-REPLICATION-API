package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.*;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.struct.saga.PossibleMatchSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.STUDENTS_NOT_FOUND;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum.PEN_MATCH_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * The type Delete possible match orchestrator test.
 */
public class DeletePossibleMatchOrchestratorTest extends BasePenReplicationAPITest {
  private final String transactionID = "1234567890";
  private final String pen1 = "120164447";
  private final String pen2 = "120146667";
  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;
  @Autowired
  private SagaService sagaService;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private DeletePossibleMatchOrchestrator orchestrator;

  @Autowired
  private AddPossibleMatchOrchestrator addPossibleMatchOrchestrator;
  @Autowired
  private MessagePublisher messagePublisher;
  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private PossibleMatchSagaData sagaData;

  /**
   * Sets up.
   *
   * @throws IOException the io exception
   */
  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    this.sagaData = PossibleMatchSagaData.builder().penTwinTransaction(this.createMockPenTwinTransaction()).build();
    this.saga = this.sagaService.createSagaRecordInDB(SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA.getCode(), "Test", JsonUtil.objectMapper.writeValueAsString(this.sagaData));
  }

  /**
   * Test delete possible match given event and saga data should post event to services api.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testDeletePossibleMatch_givenEventAndSagaData_shouldPostEventToServicesApi() throws IOException, InterruptedException, TimeoutException {
    when(this.restUtils.createStudentMapFromPenNumbers(any(), any())).thenReturn(this.createMockStudentMap());
    final var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(PEN_MATCH_API_TOPIC.getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_POSSIBLE_MATCH);
    assertThat(newEvent.getEventPayload()).isNotNull();
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(DELETE_POSSIBLE_MATCH.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  /**
   * Test delete possible match given event and saga data and students not found should post event to saga api.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testDeletePossibleMatch_givenEventAndSagaDataAndStudentsNotFound_shouldPostEventToSagaApi() throws IOException, InterruptedException, TimeoutException {
    when(this.restUtils.createStudentMapFromPenNumbers(any(), any())).thenReturn(new HashMap<>());
    final var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_POSSIBLE_MATCH);
    assertThat(newEvent.getEventOutcome()).isEqualTo(STUDENTS_NOT_FOUND);
    assertThat(newEvent.getEventPayload()).isNotNull();
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(DELETE_POSSIBLE_MATCH.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  private Map<String, Student> createMockStudentMap() {
    final Map<String, Student> studentMap = new HashMap<>();
    studentMap.put(this.pen1, this.createMockStudent(this.pen1));
    studentMap.put(this.pen2, this.createMockStudent(this.pen2));
    return studentMap;
  }

  /**
   * Test delete pen twins given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testDeletePenTwins_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    // first create Twins -- START
    final var event = Event.builder()
      .eventType(ADD_POSSIBLE_MATCH)
      .eventOutcome(EventOutcome.POSSIBLE_MATCH_ADDED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.addPossibleMatchOrchestrator.handleEvent(event);
    // creation ends
    final var event2 = Event.builder()
      .eventType(DELETE_POSSIBLE_MATCH)
      .eventOutcome(EventOutcome.POSSIBLE_MATCH_DELETED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event2);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_PEN_TWINS);
    assertThat(newEvent.getEventPayload()).isEqualTo("2");
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(DELETE_PEN_TWINS.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(2);
    assertThat(sagaStates.get(1).getSagaEventState()).isEqualTo(EventType.DELETE_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(1).getSagaEventOutcome()).isEqualTo(EventOutcome.POSSIBLE_MATCH_DELETED.toString());
  }

  /**
   * Test delete pen twins when students not found given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testDeletePenTwinsWhenStudentsNotFound_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    // first create Twins -- START
    final var event = Event.builder()
      .eventType(ADD_POSSIBLE_MATCH)
      .eventOutcome(EventOutcome.POSSIBLE_MATCH_ADDED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.addPossibleMatchOrchestrator.handleEvent(event);
    // creation ends
    final var event2 = Event.builder()
      .eventType(DELETE_POSSIBLE_MATCH)
      .eventOutcome(STUDENTS_NOT_FOUND)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event2);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(newEvent.getEventPayload()).isNull();
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(COMPLETED.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(2);
    assertThat(sagaStates.get(1).getSagaEventState()).isEqualTo(EventType.DELETE_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(1).getSagaEventOutcome()).isEqualTo(STUDENTS_NOT_FOUND.toString());
  }

  /**
   * Test update twins transaction given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testUpdateTwinsTransaction_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    this.penReplicationTestUtils.getPenTwinTransactionRepository().save(this.createMockPenTwinTransaction());
    final var event = Event.builder()
      .eventType(DELETE_PEN_TWINS)
      .eventOutcome(EventOutcome.PEN_TWINS_DELETED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_PEN_TWIN_TRANSACTION);
    assertThat(newEvent.getEventPayload()).contains("{\"transactionID\":\"1234567890\",\"transactionType\":\"" + TransactionType.DELETE_TWINS.getCode() + "\",\"transactionStatus\":\"" + TransactionStatus.COMPLETE.getCode() + "\"");
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_PEN_TWIN_TRANSACTION.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(DELETE_PEN_TWINS.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.PEN_TWINS_DELETED.toString());
  }

  /**
   * Test mark saga complete given event and saga data should post event to student api.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testMarkSagaComplete_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var event = Event.builder()
      .eventType(UPDATE_PEN_TWIN_TRANSACTION)
      .eventOutcome(EventOutcome.PEN_TWIN_TRANSACTION_UPDATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(this.sagaData.getPenTwinTransaction()))
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
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_PEN_TWIN_TRANSACTION.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.PEN_TWIN_TRANSACTION_UPDATED.toString());
  }

  private PenTwinTransaction createMockPenTwinTransaction() {
    return PenTwinTransaction.builder()
      .transactionID(this.transactionID)
      .penTwin1(this.pen1)
      .penTwin2(this.pen2)
      .transactionType(TransactionType.DELETE_TWINS.getCode())
      .transactionInsertDateTime(LocalDateTime.now())
      .twinReason("MI")
      .twinUserID("test-user")
      .transactionStatus(TransactionStatus.PENDING.getCode())
      .runDate("20210101")
      .build();
  }

  private Student createMockStudent(final String pen) {
    return Student.builder().pen(pen).studentID(UUID.randomUUID().toString()).build();
  }
}
