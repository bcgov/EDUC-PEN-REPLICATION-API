package ca.bc.gov.educ.api.pen.replication.choreographer;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.service.EventService;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_STUDENT;


/**
 * This class is responsible to handle different choreographed events related student by calling different services.
 */

@Component
@Slf4j
public class StudentChoreographer {

  private final Map<String, EventService> eventServiceMap;

  public StudentChoreographer(List<EventService> eventServices) {
    eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull Event event) throws JsonProcessingException {
    switch (event.getEventType()) {
      case "CREATE_STUDENT":
        final StudentCreate studentCreate = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
        eventServiceMap.get(CREATE_STUDENT.toString()).processEvent(studentCreate, event);
        break;
      case "UPDATE_STUDENT":
        break;
      default:
        break;

    }

  }
}
