package ca.bc.gov.educ.api.pen.replication.support;

import ca.bc.gov.educ.api.pen.replication.health.PenReplicationAPICustomHealthCheck;
import ca.bc.gov.educ.api.pen.replication.messaging.NatsConnection;
import ca.bc.gov.educ.api.pen.replication.messaging.stan.Subscriber;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import io.nats.client.Connection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * The type Mock configuration.
 */
@Profile("test")
@Configuration
public class MockConfiguration {
  /**
   * Message publisher message publisher.
   *
   * @return the message publisher
   */
  @Bean
  @Primary
  public RestUtils restUtils() {
    return Mockito.mock(RestUtils.class);
  }


  @Bean
  @Primary
  public Connection connection() {
    return Mockito.mock(Connection.class);
  }

  @Bean
  @Primary
  public NatsConnection natsConnection() {
    return Mockito.mock(NatsConnection.class);
  }

  @Bean
  @Primary
  public Subscriber subscriber() {
    return Mockito.mock(Subscriber.class);
  }

  @Bean
  @Primary
  public PenReplicationAPICustomHealthCheck penReplicationAPICustomHealthCheck() {
    return Mockito.mock(PenReplicationAPICustomHealthCheck.class);
  }

}
