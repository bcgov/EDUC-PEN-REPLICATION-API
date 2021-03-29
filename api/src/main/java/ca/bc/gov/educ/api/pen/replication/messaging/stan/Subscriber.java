package ca.bc.gov.educ.api.pen.replication.messaging.stan;

import ca.bc.gov.educ.api.pen.replication.constants.Topics;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.service.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
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
  public Subscriber(final ApplicationProperties applicationProperties, final Connection natsConnection, final EventHandlerDelegatorService eventHandlerDelegatorService) throws IOException, InterruptedException {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    final Options options = new Options.Builder()
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .connectWait(Duration.ofSeconds(30))
        .natsConn(natsConnection)
        .traceConnection()
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("pen-replication-api-subscriber" + UUID.randomUUID().toString()).build();
    this.connectionFactory = new StreamingConnectionFactory(options);
    this.connection = this.connectionFactory.createConnection();
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    final SubscriptionOptions options = new SubscriptionOptions.Builder().manualAcks().ackWait(Duration.ofMinutes(5))
        .durableName("pen-replication-api-choreography-events-consumer").build();
    for (final var topic : this.topicsToSubscribe) {
      this.connection.subscribe(topic.toString(), "pen-replication-api-choreography-events", this::onMessage, options);
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
      try {
        final String eventString = new String(message.getData());
        final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
        if (event.getEventPayload() == null) {
          message.ack();
          log.warn("payload is null, ignoring event :: {}", event);
          return;
        }
        this.subscriberExecutor.execute(() -> {
          try {
            this.eventHandlerDelegatorService.handleChoreographyEvent(event, message);
          } catch (final IOException e) {
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
   * This method will keep retrying for a connection.
   *
   * @param streamingConnection the streaming connection
   * @param e                   the e
   */
  private void connectionLostHandler(final StreamingConnection streamingConnection, final Exception e) {
    if (e != null) {
      this.reconnect();
      this.retrySubscription();
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
        this.connection = this.connectionFactory.createConnection();
        log.info("successfully reconnected after {} attempts", numOfRetries);
        break;
      } catch (final IOException ex) {
        this.backOff(numOfRetries, ex);
      } catch (final InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        this.backOff(numOfRetries, interruptedException);
      }
    }
  }

  private void retrySubscription() {
    int numOfRetries = 0;
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (final InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Back off.
   *
   * @param numOfRetries the num of retries
   * @param ex           the ex
   */
  private void backOff(final int numOfRetries, final Exception ex) {
    log.error("exception occurred", ex);
    try {
      final double sleepTime = (2 * numOfRetries);
      TimeUnit.SECONDS.sleep((long) sleepTime);
    } catch (final InterruptedException exc) {
      log.error("exception occurred", exc);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close() {
    if (this.connection != null) {
      log.info("closing stan connection...");
      try {
        this.connection.close();
      } catch (final IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
    }
  }
}
