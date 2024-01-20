package ca.bc.gov.educ.api.pen.replication.properties;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {

  public static final Executor bgTask = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("bg-task-executor-%d").build())
    .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();
  /**
   * The constant API_NAME.
   */
  public static final String API_NAME = "PEN_REPLICATION_API";

  /**
   * The Client id.
   */
  @Value("${client.id}")
  private String clientID;
  /**
   * The Client secret.
   */
  @Value("${client.secret}")
  private String clientSecret;
  /**
   * The Token url.
   */
  @Value("${url.token}")
  private String tokenURL;

  /**
   * The Stan url.
   */
  @Value("${nats.url}")
  String natsUrl;


  /**
   * The Nats max reconnect.
   */
  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;

  /**
   * The Student api url.
   */
  @Value("${url.api.student}")
  private String studentApiURL;

  @Value("${url.api.institute}")
  private String instituteApiURL;

  @Value("${url.api.independent.schools}")
  private String independentSchoolsAPI;
}
