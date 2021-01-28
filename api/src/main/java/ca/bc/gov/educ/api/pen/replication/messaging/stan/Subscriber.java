package ca.bc.gov.educ.api.pen.replication.messaging.stan;

import ca.bc.gov.educ.api.pen.replication.constants.Topics;
import ca.bc.gov.educ.api.pen.replication.messaging.NatsConnection;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.service.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber implements Closeable {

  private final Executor subscriberExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("stan-subscriber-%d").build())
      .setCorePoolSize(2).setMaximumPoolSize(2).setKeepAliveTime(Duration.ofMillis(1000)).build();
  private final EventHandlerDelegatorService eventHandlerDelegatorService;
  private final List<Topics> topicsToSubscribe = Arrays.asList(Topics.values());
  /**
   * The Connection factory.
   */
  private final StreamingConnectionFactory connectionFactory;

  /**
   * The Connection.
   */
  private StreamingConnection connection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param applicationProperties        the application properties
   * @param natsConnection               the nats connection
   * @param eventHandlerDelegatorService the event handler delegator service
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public Subscriber(ApplicationProperties applicationProperties, NatsConnection natsConnection, EventHandlerDelegatorService eventHandlerDelegatorService) throws IOException, InterruptedException {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    Options options = new Options.Builder()
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .natsConn(natsConnection.getNatsCon())
        .traceConnection()
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("pen-replication-api-subscriber" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   *
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    SubscriptionOptions options = new SubscriptionOptions.Builder().manualAcks().ackWait(Duration.ofMinutes(5))
        .durableName("pen-replication-api-choreography-events-consumer").build();
    topicsToSubscribe.forEach(topic -> {
      try {
        connection.subscribe(topic.toString(), "pen-replication-api-choreography-events", this::onMessage, options);
      } catch (IOException | TimeoutException e) {
        log.error("IOException | TimeoutException ", e);
      } catch (InterruptedException e) {
        log.error("InterruptedException ", e);
        Thread.currentThread().interrupt();
      }
    });

  }

  /**
   * This method will process the event message pushed into different topics of different APIS.
   * All APIs publish ChoreographedEvent
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onMessage(Message message) {
    if (message != null) {
      try {
        String eventString = new String(message.getData());
        ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
        if (event.getEventPayload() == null) {
          message.ack();
          log.warn("payload is null, ignoring event :: {}", event);
          return;
        }
        subscriberExecutor.execute(() -> {
          try {
            eventHandlerDelegatorService.handleChoreographyEvent(event, message);
          } catch (IOException e) {
            log.error("IOException ", e);
          }
        });
        log.info("received event :: {} ", event);
      } catch (final Exception ex) {
        log.error("Exception ", ex);
      }
    }
  }


  /**
   * Retry subscription.
   */
  private void retrySubscription() {
    int numOfRetries = 0;
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * This method will keep retrying for a connection.
   *
   * @param streamingConnection the streaming connection
   * @param e                   the e
   */
  private void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    if (e != null) {
      reconnect();
      retrySubscription();
    }
  }

  /**
   * Reconnect.
   */
  private void reconnect() {
    int numOfRetries = 1;
    while (true) {
      try {
        log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
        connection = connectionFactory.createConnection();
        log.info("successfully reconnected after {} attempts", numOfRetries);
        break;
      } catch (IOException ex) {
        backOff(numOfRetries, ex);
      } catch (InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        backOff(numOfRetries, interruptedException);
      }
    }
  }

  /**
   * Back off.
   *
   * @param numOfRetries the num of retries
   * @param ex           the ex
   */
  private void backOff(int numOfRetries, Exception ex) {
    log.error("exception occurred", ex);
    try {
      double sleepTime = (2 * numOfRetries);
      TimeUnit.SECONDS.sleep((long) sleepTime);
    } catch (InterruptedException exc) {
      log.error("exception occurred", exc);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close() {
    if (connection != null) {
      log.info("closing stan connection...");
      try {
        connection.close();
      } catch (IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
    }
  }
}
