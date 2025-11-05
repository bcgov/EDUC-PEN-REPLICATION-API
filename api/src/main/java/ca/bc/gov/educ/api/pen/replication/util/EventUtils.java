package ca.bc.gov.educ.api.pen.replication.util;

import ca.bc.gov.educ.api.pen.replication.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.exception.IgnoreEventException;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEventValidation;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.EventValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;


public final class EventUtils {
  private EventUtils() {
  }

  public static ChoreographedEvent getChoreographedEventIfValid(String eventString) throws JsonProcessingException, IgnoreEventException {
    final ChoreographedEventValidation event = JsonUtil.getJsonObjectFromString(ChoreographedEventValidation.class, eventString);
    if(StringUtils.isNotBlank(event.getEventOutcome()) && !EventOutcome.isValid(event.getEventOutcome())) {
      throw new IgnoreEventException("Invalid event outcome", event.getEventType(), event.getEventOutcome());
    }else if(StringUtils.isNotBlank(event.getEventType()) && !EventType.isValid(event.getEventType())) {
      throw new IgnoreEventException("Invalid event type", event.getEventType(), event.getEventOutcome());
    }
    return JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
  }

  public static Event getEventIfValid(String eventString) throws JsonProcessingException, IgnoreEventException {
    final EventValidation event = JsonUtil.getJsonObjectFromString(EventValidation.class, eventString);
    if(StringUtils.isNotBlank(event.getEventType()) && !EventType.isValid(event.getEventType())) {
      throw new IgnoreEventException("Invalid event type", event.getEventType(), event.getEventOutcome());
    } else if(StringUtils.isNotBlank(event.getEventOutcome()) && !EventOutcome.isValid(event.getEventOutcome())) {
      throw new IgnoreEventException("Invalid event outcome", event.getEventType(), event.getEventOutcome());
    }
    return JsonUtil.getJsonObjectFromString(Event.class, eventString);
  }

}
