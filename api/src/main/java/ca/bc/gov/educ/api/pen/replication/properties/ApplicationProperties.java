package ca.bc.gov.educ.api.pen.replication.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {
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
  @Value("${stan.url}")
  String stanUrl;

  /**
   * The Stan cluster.
   */
  @Value("${stan.cluster}")
  String stanCluster;

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
}
