package ca.bc.gov.educ.api.pen.replication.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

/**
 * The type Test redis configuration.
 */
@TestConfiguration
public class TestRedisConfiguration {

  private final RedisServer redisServer;

  /**
   * Instantiates a new Test redis configuration.
   */
  public TestRedisConfiguration() {
    this.redisServer = RedisServer.builder().port(6370).build();
  }

  /**
   * Post construct.
   */
  @PostConstruct
  public void postConstruct() {
    this.redisServer.start();
  }

  /**
   * Pre destroy.
   */
  @PreDestroy
  public void preDestroy() {
    this.redisServer.stop();
  }
}
