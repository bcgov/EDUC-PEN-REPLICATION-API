package ca.bc.gov.educ.api.pen.replication.orchestrator.base;

import ca.bc.gov.educ.api.pen.replication.constants.*;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.NotificationEvent;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.COMPLETED;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

/**
 * The type Base orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseOrchestrator<T> implements EventHandler, Orchestrator {

  /**
   * The constant SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT.
   */
  protected static final String SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT = "system is going to execute next event :: {} for current event {} and SAGA ID :: {}";
  /**
   * The constant SELF
   */
  protected static final String SELF = "SELF";
  /**
   * The Clazz.
   */
  protected final Class<T> clazz;
  /**
   * The Next steps to execute.
   */
  protected final Map<EventType, List<SagaEventState<T>>> nextStepsToExecute = new LinkedHashMap<>();
  private final EntityManagerFactory emf;
  /**
   * The Saga service.
   */
  @Getter(PROTECTED)
  private final SagaService sagaService;
  /**
   * The Message publisher.
   */
  @Getter(PROTECTED)
  private final MessagePublisher messagePublisher;
  /**
   * The Saga name.
   */
  @Getter(PUBLIC)
  private final SagaEnum sagaName;
  /**
   * The Topic to subscribe.
   */
  @Getter(PUBLIC)
  private final SagaTopicsEnum topicToSubscribe;
  /**
   * The flag to indicate whether t
   */
  @Setter(PROTECTED)
  protected boolean shouldSendNotificationEvent = true;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param emf              the entity manager factory
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param clazz            the clazz
   * @param sagaName         the saga name
   * @param topicToSubscribe the topic to subscribe
   */
  protected BaseOrchestrator(final EntityManagerFactory emf, final SagaService sagaService, final MessagePublisher messagePublisher,
                             final Class<T> clazz, final SagaEnum sagaName,
                             final SagaTopicsEnum topicToSubscribe) {
    this.emf = emf;
    this.sagaService = sagaService;
    this.messagePublisher = messagePublisher;
    this.clazz = clazz;
    this.sagaName = sagaName;
    this.topicToSubscribe = topicToSubscribe;
    this.populateStepsToExecuteMap();
  }

  /**
   * Create single collection event state list.
   *
   * @param eventOutcome      the event outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEventType     the next event type
   * @param stepToExecute     the step to execute
   * @return the list
   */
  protected List<SagaEventState<T>> createSingleCollectionEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    final List<SagaEventState<T>> eventStates = new ArrayList<>();
    eventStates.add(this.buildSagaEventState(eventOutcome, nextStepPredicate, nextEventType, stepToExecute));
    return eventStates;
  }


  /**
   * Build saga event state saga event state.
   *
   * @param eventOutcome      the event outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEventType     the next event type
   * @param stepToExecute     the step to execute
   * @return the saga event state
   */
  protected SagaEventState<T> buildSagaEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    return SagaEventState.<T>builder().currentEventOutcome(eventOutcome).nextStepPredicate(nextStepPredicate).nextEventType(nextEventType).stepToExecute(stepToExecute).build();
  }


  /**
   * Register step to execute base orchestrator.
   *
   * @param initEvent         the init event
   * @param outcome           the outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEvent         the next event
   * @param stepToExecute     the step to execute
   * @return the base orchestrator
   */
  protected BaseOrchestrator<T> registerStepToExecute(final EventType initEvent, final EventOutcome outcome, final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    if (this.nextStepsToExecute.containsKey(initEvent)) {
      final List<SagaEventState<T>> states = this.nextStepsToExecute.get(initEvent);
      states.add(this.buildSagaEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    } else {
      this.nextStepsToExecute.put(initEvent, this.createSingleCollectionEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    }
    return this;
  }

  /**
   * Step base orchestrator.
   *
   * @param currentEvent  the event that has occurred.
   * @param outcome       outcome of the event.
   * @param nextEvent     next event that will occur.
   * @param stepToExecute which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> step(final EventType currentEvent, final EventOutcome outcome, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, nextEvent, stepToExecute);
  }

  /**
   * Step base orchestrator.
   *
   * @param currentEvent      the event that has occurred.
   * @param outcome           outcome of the event.
   * @param nextStepPredicate whether to execute the next step.
   * @param nextEvent         next event that will occur.
   * @param stepToExecute     which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> step(final EventType currentEvent, final EventOutcome outcome, final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, nextStepPredicate, nextEvent, stepToExecute);
  }

  /**
   * Beginning step base orchestrator.
   *
   * @param nextEvent     next event that will occur.
   * @param stepToExecute which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> begin(final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(INITIATED, INITIATE_SUCCESS, (T sagaData) -> true, nextEvent, stepToExecute);
  }

  /**
   * Beginning step base orchestrator.
   *
   * @param nextStepPredicate whether to execute the next step.
   * @param nextEvent         next event that will occur.
   * @param stepToExecute     which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> begin(final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(INITIATED, INITIATE_SUCCESS, nextStepPredicate, nextEvent, stepToExecute);
  }

  /**
   * End step base orchestrator with complete status.
   *
   * @param currentEvent the event that has occurred.
   * @param outcome      outcome of the event.
   */
  public BaseOrchestrator<T> end(final EventType currentEvent, final EventOutcome outcome) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }

  /**
   * End step with method to execute with complete status.
   *
   * @param currentEvent  the event that has occurred.
   * @param outcome       outcome of the event.
   * @param stepToExecute which method to execute for the MARK_SAGA_COMPLETE event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> end(final EventType currentEvent, final EventOutcome outcome, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, MARK_SAGA_COMPLETE, (Event event, Saga saga, T sagaData) -> {
      stepToExecute.apply(event, saga, sagaData);
      this.markSagaComplete(event, saga, sagaData);
    });
  }

  /**
   * Syntax sugar to make the step statement expressive
   *
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> or() {
    return this;
  }

  /**
   * this is a simple and convenient method to trigger builder pattern in the child classes.
   *
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> stepBuilder() {
    return this;
  }

  /**
   * this method will check if the event is not already processed. this could happen in SAGAs due to duplicate messages.
   * Application should be able to handle this.
   *
   * @param currentEventType current event.
   * @param saga             the model object.
   * @param eventTypes       event types stored in the hashmap
   * @return true or false based on whether the current event with outcome received from the queue is already processed or not.
   */
  protected boolean isNotProcessedEvent(final EventType currentEventType, final Saga saga, final Set<EventType> eventTypes) {
    val eventTypeInDB = EventType.valueOf(saga.getSagaState());
    val events = new LinkedList<>(eventTypes);
    val dbEventIndex = events.indexOf(eventTypeInDB);
    val currentEventIndex = events.indexOf(currentEventType);
    return currentEventIndex >= dbEventIndex;
  }

  /**
   * creates the PenRequestSagaEventState object
   *
   * @param saga         the payload.
   * @param eventType    event type
   * @param eventOutcome outcome
   * @param eventPayload payload.
   * @return {@link SagaEvent}
   */
  protected SagaEvent createEventState(@NotNull final Saga saga, @NotNull final EventType eventType, @NotNull final EventOutcome eventOutcome, final String eventPayload) {
    final var user = this.sagaName.getCode().length() > 32 ? this.sagaName.getCode().substring(0, 32) : this.sagaName.getCode();
    return SagaEvent.builder()
      .createDate(LocalDateTime.now())
      .createUser(user)
      .updateDate(LocalDateTime.now())
      .updateUser(user)
      .saga(saga)
      .sagaEventOutcome(eventOutcome.toString())
      .sagaEventState(eventType.toString())
      .sagaStepNumber(this.calculateStep(saga))
      .sagaEventResponse(eventPayload == null ? " " : eventPayload)
      .build();
  }

  /**
   * This method updates the DB and marks the process as complete.
   *
   * @param event    the current event.
   * @param saga     the saga model object.
   * @param sagaData the payload string as object.
   */
  protected void markSagaComplete(final Event event, final Saga saga, final T sagaData) {
    log.trace("payload is {}", sagaData);
    if (this.shouldSendNotificationEvent) {
      final var finalEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, finalEvent);
      finalEvent.setEventType(MARK_SAGA_COMPLETE);
      finalEvent.setEventOutcome(SAGA_COMPLETED);
      finalEvent.setSagaStatus(COMPLETED.toString());
      finalEvent.setSagaName(this.getSagaName().getCode());
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), finalEvent);
    }

    val sagaEvent = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(COMPLETED.toString());
    saga.setStatus(COMPLETED.toString());
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaService().updateAttachedSagaWithEvents(saga, sagaEvent);

  }

  /**
   * calculate step number
   *
   * @param saga the model object.
   * @return step number that was calculated.
   */
  private int calculateStep(final Saga saga) {
    val sagaStates = this.getSagaService().findAllSagaStates(saga);
    return (sagaStates.size() + 1);
  }

  /**
   * convenient method to post message to topic, to be used by child classes.
   *
   * @param topicName topic name where the message will be posted.
   * @param nextEvent the next event object.
   */
  protected void postMessageToTopic(final String topicName, final Event nextEvent) {
    final var eventStringOptional = JsonUtil.getJsonString(nextEvent);
    if (eventStringOptional.isPresent()) {
      this.getMessagePublisher().dispatchMessage(topicName, eventStringOptional.get().getBytes());
    } else {
      log.error("event string is not present for  :: {} :: this should not have happened", nextEvent);
    }
  }

  /**
   * it finds the last event that was processed successfully for this saga.
   *
   * @param eventStates event states corresponding to the Saga.
   * @return {@link SagaEvent} if found else null.
   */
  protected Optional<SagaEvent> findTheLastEventOccurred(final List<SagaEvent> eventStates) {
    final int step = eventStates.stream().map(SagaEvent::getSagaStepNumber).mapToInt(x -> x).max().orElse(0);
    return eventStates.stream().filter(element -> element.getSagaStepNumber() == step).findFirst();
  }

  /**
   * this method is called from the cron job , which will replay the saga process based on its current state.
   *
   * @param saga the model object.
   * @throws IOException          if there is connectivity problem
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   */
  @Override
  @Async("asyncTaskExecutor")
  public void replaySaga(final Saga saga) throws IOException, InterruptedException, TimeoutException {
    final var eventStates = this.getSagaService().findAllSagaStates(saga);
    final var t = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
    if (eventStates.isEmpty()) { //process did not start last time, lets start from beginning.
      this.replayFromBeginning(saga, t);
    } else {
      this.replayFromLastEvent(saga, eventStates, t);
    }
  }

  /**
   * This method will restart the saga process from where it was left the last time. which could occur due to various reasons
   *
   * @param saga        the model object.
   * @param eventStates the event states corresponding to the saga
   * @param t           the payload string as an object
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  private void replayFromLastEvent(final Saga saga, final List<SagaEvent> eventStates, final T t) throws InterruptedException, TimeoutException, IOException {
    val sagaEventOptional = this.findTheLastEventOccurred(eventStates);
    if (sagaEventOptional.isPresent()) {
      val sagaEvent = sagaEventOptional.get();
      log.trace(sagaEventOptional.toString());
      final EventType currentEvent = EventType.valueOf(sagaEvent.getSagaEventState());
      final EventOutcome eventOutcome = EventOutcome.valueOf(sagaEvent.getSagaEventOutcome());
      final Event event = Event.builder()
        .eventOutcome(eventOutcome)
        .eventType(currentEvent)
        .eventPayload(sagaEvent.getSagaEventResponse())
        .build();
      final Optional<SagaEventState<T>> sagaEventState = this.findNextSagaEventState(currentEvent, eventOutcome, t);
      if (sagaEventState.isPresent()) {
        log.trace(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.get().getNextEventType(), event.toString(), saga.getSagaId());
        this.invokeNextEvent(event, saga, t, sagaEventState.get());
      }
    }
  }

  /**
   * This method will restart the saga process from the beginning. which could occur due to various reasons
   *
   * @param saga the model object.
   * @param t    the payload string as an object
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  private void replayFromBeginning(final Saga saga, final T t) throws InterruptedException, TimeoutException, IOException {
    val event = Event.builder()
      .eventOutcome(INITIATE_SUCCESS)
      .eventType(INITIATED)
      .build();
    val sagaEventState = this.findNextSagaEventState(INITIATED, INITIATE_SUCCESS, t);
    if (sagaEventState.isPresent()) {
      log.trace(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.get().getNextEventType(), event.toString(), saga.getSagaId());
      this.invokeNextEvent(event, saga, t, sagaEventState.get());
    }
  }

  /**
   * this method is called if there is a new message on this specific topic which this service is listening.
   *
   * @param event the event
   * @throws InterruptedException if thread is interrupted.
   * @throws IOException          if there is connectivity problem
   * @throws TimeoutException     if connection to messaging system times out.
   */
  @Override
  @Async("subscriberExecutor")
  public void handleEvent(@NotNull final Event event) throws InterruptedException, IOException, TimeoutException {
    log.info("executing saga event {}", event.getEventType());
    log.trace("Full event :: {}", event);
    if (this.sagaEventExecutionNotRequired(event)) {
      log.trace("Execution is not required for this message returning EVENT is :: {}", event);
      return;
    }
    this.broadcastSagaInitiatedMessage(event);

    final var sagaOptional = this.getSagaService().findSagaById(event.getSagaId()); // system expects a saga record to be present here.
    if (sagaOptional.isPresent()) {
      val saga = sagaOptional.get();
      if (!COMPLETED.toString().equalsIgnoreCase(sagaOptional.get().getStatus())) {//possible duplicate message or force stop scenario check
        final T sagaData = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
        final var sagaEventState = this.findNextSagaEventState(event.getEventType(), event.getEventOutcome(), sagaData);
        log.trace("found next event as {}", sagaEventState);
        if (sagaEventState.isPresent()) {
          this.process(event, saga, sagaData, sagaEventState.get());
        } else {
          log.error("This should not have happened, please check that both the saga api and all the participating apis are in sync in terms of events and their outcomes. {}", event); // more explicit error message,
        }
      } else {
        log.info("got message to process saga for saga ID :: {} but saga is already :: {}", saga.getSagaId(), saga.getStatus());
      }
    } else {
      log.error("Saga process without DB record is not expected. {}", event);
    }
  }

  /**
   * Start to execute saga
   *
   * @param saga the saga data
   */
  @Override
  @Async("subscriberExecutor")
  public void startSaga(@NotNull final Saga saga) {
    try {
      this.handleEvent(Event.builder()
        .eventType(INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(saga.getPayload())
        .build());
    } catch (final InterruptedException e) {
      log.error("InterruptedException while startSaga", e);
      Thread.currentThread().interrupt();
    } catch (final TimeoutException | IOException e) {
      log.error("Exception while startSaga", e);
    }
  }


  /**
   * DONT DO ANYTHING the message was broad-casted for the frontend listeners, that a saga process has initiated, completed.
   *
   * @param event the event object received from queue.
   * @return true if this message need not be processed further.
   */
  private boolean sagaEventExecutionNotRequired(@NotNull final Event event) {
    return (event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS && SELF.equalsIgnoreCase(event.getReplyTo()))
      || event.getEventType() == MARK_SAGA_COMPLETE && event.getEventOutcome() == SAGA_COMPLETED;
  }

  /**
   * Broadcast the saga initiated message
   *
   * @param event the event object
   */
  private void broadcastSagaInitiatedMessage(@NotNull final Event event) {
    // !SELF.equalsIgnoreCase(event.getReplyTo()):- this check makes sure it is not broadcast-ed infinitely.
    if (this.shouldSendNotificationEvent && event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS
      && !SELF.equalsIgnoreCase(event.getReplyTo())) {
      final var notificationEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, notificationEvent);
      notificationEvent.setSagaStatus(INITIATED.toString());
      notificationEvent.setReplyTo(SELF);
      notificationEvent.setSagaName(this.getSagaName().getCode());
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), notificationEvent);
    }
  }

  /**
   * this method finds the next event that needs to be executed.
   *
   * @param currentEvent current event
   * @param eventOutcome event outcome.
   * @param sagaData     the saga data
   * @return {@link Optional<SagaEventState>}
   */
  protected Optional<SagaEventState<T>> findNextSagaEventState(final EventType currentEvent, final EventOutcome eventOutcome, final T sagaData) {
    val sagaEventStates = this.nextStepsToExecute.get(currentEvent);
    return sagaEventStates == null ? Optional.empty() : sagaEventStates.stream().filter(el ->
      el.getCurrentEventOutcome() == eventOutcome && el.nextStepPredicate.test(sagaData)
    ).findFirst();
  }

  /**
   * this method starts the process of saga event execution.
   *
   * @param event          the current event.
   * @param saga           the model object.
   * @param sagaData       the saga data
   * @param sagaEventState the next next event from {@link BaseOrchestrator#nextStepsToExecute}
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  protected void process(@NotNull final Event event, final Saga saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    if (!saga.getSagaState().equalsIgnoreCase(COMPLETED.toString())
      && this.isNotProcessedEvent(event.getEventType(), saga, this.nextStepsToExecute.keySet())) {
      log.info(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.getNextEventType(), event.getEventType(), saga.getSagaId());
      log.trace("Full event for SAGA_ID :: {} is :: {}", saga.getSagaId(), event);
      this.invokeNextEvent(event, saga, sagaData, sagaEventState);
    } else {
      log.info("ignoring this message as we have already processed it or it is completed. {}", event.toString()); // it is expected to receive duplicate message in saga pattern, system should be designed to handle duplicates.
    }
  }

  /**
   * this method will invoke the next event in the {@link BaseOrchestrator#nextStepsToExecute}
   *
   * @param event          the current event.
   * @param saga           the model object.
   * @param sagaData       the payload string
   * @param sagaEventState the next next event from {@link BaseOrchestrator#nextStepsToExecute}
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  protected void invokeNextEvent(final Event event, final Saga saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    final SagaStep<T> stepToExecute = sagaEventState.getStepToExecute();
    stepToExecute.apply(event, saga, sagaData);
  }

  /**
   * Populate steps to execute map.
   */
  public abstract void populateStepsToExecuteMap();

  /**
   * Persist data natively.
   *
   * @param query the query
   * @return the int
   */
  protected int persistData(final String query) {
    val em = this.emf.createEntityManager();
    val tx = em.getTransaction();
    var rowsAffected = 0;
    try {
      tx.begin();
      rowsAffected = em.createNativeQuery(query).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
      tx.commit();
    } catch (final Exception e) {
      log.error("Error occurred saving entity " + e.getMessage());
      if (tx.isActive()) {
        try {
          tx.rollback();
        } catch (final IllegalStateException | PersistenceException ex) {
          log.error("IllegalStateException | PersistenceException", ex);
        }
      }
      throw e;
    } finally {
      if (em.isOpen()) {
        em.close();
      }
    }
    return rowsAffected;
  }

  /**
   * Update pen demog transaction.
   *
   * @param event                         the event
   * @param saga                          the saga
   * @param penDemogTx                    the pen demog tx
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @throws JsonProcessingException the json processing exception
   */
  protected void updatePenDemogTransaction(final Event event, final Saga saga, final PenDemogTransaction penDemogTx, final PenDemogTransactionRepository penDemogTransactionRepository) throws JsonProcessingException {
    saga.setSagaState(UPDATE_PEN_DEMOG_TRANSACTION.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    val pendDemogTxOptional = penDemogTransactionRepository.findById(penDemogTx.getTransactionID());
    if (pendDemogTxOptional.isPresent()) {
      val penDemogTxUpdated = pendDemogTxOptional.get();
      penDemogTxUpdated.setTransactionStatus(TransactionStatus.COMPLETE.getCode());
      penDemogTxUpdated.setTransactionProcessedDateTime(LocalDateTime.now());
      penDemogTransactionRepository.save(penDemogTxUpdated);
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(UPDATE_PEN_DEMOG_TRANSACTION)
        .eventOutcome(PEN_DEMOG_TRANSACTION_UPDATED)
        .eventPayload(JsonUtil.getJsonStringFromObject(penDemogTxUpdated))
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
      log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), ADD_PEN_DEMOG, saga.getSagaId());
    } else {
      log.error("This should not have happened as it is not expected to have a saga running without a transaction in Pen Demog Transaction. Transaction ID :: {}", penDemogTx.getTransactionID());
    }
  }


  /**
   * Update pen twin transaction.
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param penTwinTransaction           the pen twin transaction
   * @param penTwinTransactionRepository the pen twin transaction repository
   * @throws JsonProcessingException the json processing exception
   */
  protected void updatePenTwinTransaction(final Event event, final Saga saga, final PenTwinTransaction penTwinTransaction, final PenTwinTransactionRepository penTwinTransactionRepository) throws JsonProcessingException {
    saga.setSagaState(UPDATE_PEN_TWIN_TRANSACTION.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    val penTwinTransactionOptional = penTwinTransactionRepository.findById(penTwinTransaction.getTransactionID());
    if (penTwinTransactionOptional.isPresent()) {
      val twinTransaction = penTwinTransactionOptional.get();
      twinTransaction.setTransactionStatus(TransactionStatus.COMPLETE.getCode());
      twinTransaction.setTransactionProcessedDateTime(LocalDateTime.now());
      penTwinTransactionRepository.save(twinTransaction);
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(UPDATE_PEN_TWIN_TRANSACTION)
        .eventOutcome(PEN_TWIN_TRANSACTION_UPDATED)
        .eventPayload(JsonUtil.getJsonStringFromObject(twinTransaction))
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent); // this will make it async and use pub/sub flow even though it is sending message to itself
      log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), ADD_PEN_DEMOG, saga.getSagaId());
    } else {
      log.error("This should not have happened as it is not expected to have a saga running without a transaction in Pen Twin Transaction. Transaction ID :: {}", penTwinTransaction.getTransactionID());
    }
  }

  protected void updatePenTwinTransactionToError(final PenTwinTransaction penTwinTransaction, final PenTwinTransactionRepository penTwinTransactionRepository) {
    val penTwinTransactionOptional = penTwinTransactionRepository.findById(penTwinTransaction.getTransactionID());
    if (penTwinTransactionOptional.isPresent()) {
      val twinTransaction = penTwinTransactionOptional.get();
      twinTransaction.setTransactionStatus(TransactionStatus.ERROR.getCode());
      twinTransaction.setTransactionProcessedDateTime(LocalDateTime.now());
      penTwinTransactionRepository.save(twinTransaction);
    }
  }
}
