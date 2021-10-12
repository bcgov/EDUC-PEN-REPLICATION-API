package ca.bc.gov.educ.api.pen.replication.support;

import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.messaging.MessageSubscriber;
import ca.bc.gov.educ.api.pen.replication.messaging.NatsConnection;
import ca.bc.gov.educ.api.pen.replication.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import io.nats.client.Connection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * The type Mock configuration.
 */
@Profile("test-health-check")
@Configuration
public class MockConfigurationHealthCheck {
  /**
   * Connection connection.
   *
   * @return the connection
   */
  @Bean
  @Primary
  public Connection connection() {
    return Mockito.mock(Connection.class);
  }

  /**
   * Nats connection nats connection.
   *
   * @return the nats connection
   */
  @Bean
  @Primary
  public NatsConnection natsConnection() {
    return Mockito.mock(NatsConnection.class);
  }

  /**
   * Message publisher message publisher.
   *
   * @return the message publisher
   */
  @Bean
  @Primary
  public MessagePublisher messagePublisher() {
    return Mockito.mock(MessagePublisher.class);
  }

  /**
   * Subscriber subscriber.
   *
   * @return the subscriber
   */
  @Bean
  @Primary
  public Subscriber subscriber() {
    return Mockito.mock(Subscriber.class);
  }


  /**
   * Rest template rest template.
   *
   * @return the rest template
   */
  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return Mockito.mock(RestTemplate.class);
  }

  /**
   * Rest utils rest utils.
   *
   * @return the rest utils
   */
  @Bean
  @Primary
  public RestUtils restUtils() {
    return Mockito.mock(RestUtils.class);
  }


  /**
   * Nats connection nats connection.
   *
   * @return the nats connection
   */


  /**
   * Message subscriber message subscriber.
   *
   * @return the message subscriber
   */
  @Bean
  @Primary
  public MessageSubscriber messageSubscriber() {
    return Mockito.mock(MessageSubscriber.class);
  }
}
