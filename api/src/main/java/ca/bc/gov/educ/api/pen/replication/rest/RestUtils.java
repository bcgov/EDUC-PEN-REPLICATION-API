package ca.bc.gov.educ.api.pen.replication.rest;

import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.struct.BaseStudent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  private final ApplicationProperties props;

  /**
   * The Web client.
   */
  private final WebClient webClient;

  /**
   * Instantiates a new Rest utils.
   *
   * @param props the props
   */
  @Autowired
  public RestUtils(final ApplicationProperties props, final WebClient webClient) {
    this.props = props;
    this.webClient = webClient;
  }

  @Retryable(value = {Exception.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Optional<String> getStudentTruePen(String trueStudentID) {
    var studentResponse = webClient.get().uri(props.getStudentApiURL() + "/" + trueStudentID).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToMono(BaseStudent.class).block();
    if (studentResponse != null && studentResponse.getPen() != null) {
      return Optional.of(studentResponse.getPen());
    }
    return Optional.empty();
  }


}
