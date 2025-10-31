package ca.bc.gov.educ.api.pen.replication.messaging.jetstream;

import ca.bc.gov.educ.api.pen.replication.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.service.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber {

  private final Executor subscriberExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("jet-stream-subscriber-%d").build())
      .setCorePoolSize(2).setMaximumPoolSize(2).setKeepAliveTime(Duration.ofMillis(1000)).build();
  private final EventHandlerDelegatorService eventHandlerDelegatorService;
  private final Map<String, List<String>> streamTopicsMap = new HashMap<>(); // one stream can have multiple topics.
  private final Connection natsConnection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection               the nats connection
   * @param eventHandlerDelegatorService the event handler delegator service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final EventHandlerDelegatorService eventHandlerDelegatorService) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    this.natsConnection = natsConnection;
    this.initializeStreamTopicMap();
  }

  /**
   * this is the source of truth for all the topics this api subscribes to.
   */
  private void initializeStreamTopicMap() {
    final List<String> penServicesEventsTopics = new ArrayList<>();
    penServicesEventsTopics.add("PEN_SERVICES_EVENTS_TOPIC");
    final List<String> penMatchEventsTopics = new ArrayList<>();
    penMatchEventsTopics.add("PEN_MATCH_EVENTS_TOPIC");
    final List<String> studentEventsTopics = new ArrayList<>();
    studentEventsTopics.add("STUDENT_EVENTS_TOPIC");
    final List<String> instituteEventsTopics = new ArrayList<>();
    instituteEventsTopics.add("INSTITUTE_EVENTS_TOPIC");
    final List<String> gradStatusEventsTopics = new ArrayList<>();
    gradStatusEventsTopics.add("GRAD_STATUS_EVENT_TOPIC");
    final List<String> scholarshipsEventsTopics = new ArrayList<>();
    scholarshipsEventsTopics.add("SCHOLARSHIPS_EVENTS_TOPIC");
    this.streamTopicsMap.put("PEN_SERVICES_EVENTS", penServicesEventsTopics);
    this.streamTopicsMap.put("STUDENT_EVENTS", studentEventsTopics);
    this.streamTopicsMap.put("PEN_MATCH_EVENTS", penMatchEventsTopics);
    this.streamTopicsMap.put("INSTITUTE_EVENTS", instituteEventsTopics);
    this.streamTopicsMap.put("SCHOLARSHIPS_EVENTS", scholarshipsEventsTopics);
    this.streamTopicsMap.put("GRAD_STATUS_EVENT_STREAM", gradStatusEventsTopics);
  }


  /**
   * Subscribe.
   *
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = ApplicationProperties.API_NAME.concat("-QUEUE");
    val autoAck = false;
    for (val entry : this.streamTopicsMap.entrySet()) {
      for (val topic : entry.getValue()) {
        final PushSubscribeOptions options = PushSubscribeOptions.builder().stream(entry.getKey())
          .durable(ApplicationProperties.API_NAME.concat("-DURABLE"))
          .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
        log.info("Subscribing to topic: " + topic);
        this.natsConnection.jetStream().subscribe(topic, qName, this.natsConnection.createDispatcher(), this::onMessage,
          autoAck, options);
      }
    }
  }


  /**
   * This method will process the event message pushed into different topics of different APIS.
   * All APIs publish ChoreographedEvent
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onMessage(final Message message) {
    if (message != null) {
//      log.info("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
//      try {
//        val eventString = new String(message.getData());
//        LogHelper.logMessagingEventDetails(eventString);
//        final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
//        if (event.getEventPayload() == null) {
//          message.ack();
//          log.warn("payload is null, ignoring event :: {}", event);
//          return;
//        }
//        this.subscriberExecutor.execute(() -> {
//          try {
//            this.eventHandlerDelegatorService.handleChoreographyEvent(event, message);
//          } catch (final IOException e) {
//            log.error("IOException ", e);
//          }
//        });
//        log.info("received event :: {} ", event);
//      } catch (final Exception ex) {
//        log.error("Exception ", ex);
//      }
      log.info("Received message: " + JsonUtil.getJsonString(message));
      message.ack();
      log.info("Message acknowledged");
    }
  }

}
