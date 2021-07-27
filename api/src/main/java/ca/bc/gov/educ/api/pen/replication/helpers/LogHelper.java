package ca.bc.gov.educ.api.pen.replication.helpers;

import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Log helper.
 */
@Slf4j
public final class LogHelper {
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

  public static void logSagaRetry(final Saga saga) {
    final Map<String, Object> retrySagaMap = new HashMap<>();
    try {
      retrySagaMap.put("sagaName", saga.getSagaName());
      retrySagaMap.put("sagaId", saga.getSagaId());
      retrySagaMap.put("retryCount", saga.getRetryCount());
      MDC.putCloseable("sagaRetry", JsonUtil.objectMapper.writeValueAsString(retrySagaMap));
      log.info("Saga is being retried.");
    } catch (final Exception ex) {
      log.error(EXCEPTION, ex);
    }
  }
}
