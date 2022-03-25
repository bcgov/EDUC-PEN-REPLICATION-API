package ca.bc.gov.educ.api.pen.replication.support;

import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
    this.redisServer = RedisServer.builder().port(6370).setting("maxheap 50M").build();
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
