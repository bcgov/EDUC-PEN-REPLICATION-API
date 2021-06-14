package ca.bc.gov.educ.api.pen.replication.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public final class LogHelper {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String EXCEPTION = "Exception ";

  private LogHelper() {

  }



  /**
   * the event is a json string.
   *
   * @param event the json string
   */
  public static void logMessagingEventDetails(final String event) {
    try {
      MDC.putCloseable("messageEvent", event);
      log.info("");
      MDC.clear();
    } catch (final Exception exception) {
      log.error(EXCEPTION, exception);
    }
  }
}
