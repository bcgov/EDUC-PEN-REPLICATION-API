package ca.bc.gov.educ.api.pen.replication.health;

import io.nats.client.Connection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The type Pen match api custom health check.
 */
@Component
public class PenReplicationAPICustomHealthCheck implements HealthIndicator {
  /**
   * The Nats connection.
   */
  private final Connection natsConnection;

  /**
   * Instantiates a new Pen match api custom health check.
   *
   * @param natsConnection the nats connection
   */
  public PenReplicationAPICustomHealthCheck(Connection natsConnection) {
    this.natsConnection = natsConnection;
  }

  @Override
  public Health getHealth(boolean includeDetails) {
    return healthCheck();
  }


  @Override
  public Health health() {
    return healthCheck();
  }

  /**
   * Health check health.
   *
   * @return the health
   */
  private Health healthCheck() {
    if (this.natsConnection.getStatus() == Connection.Status.CLOSED) {
      return Health.down().withDetail("NATS", " Connection is Closed.").build();
    }
    return Health.up().build();
  }
}
