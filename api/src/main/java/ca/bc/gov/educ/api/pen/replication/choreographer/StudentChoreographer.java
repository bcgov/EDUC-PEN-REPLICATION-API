package ca.bc.gov.educ.api.pen.replication.choreographer;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.service.EventService;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_STUDENT;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.UPDATE_STUDENT;


/**
 * This class is responsible to handle different choreographed events related student by calling different services.
 */

@Component
@Slf4j
public class StudentChoreographer {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
      .setCorePoolSize(1).setMaximumPoolSize(1).build();
  private final Map<String, EventService> eventServiceMap;

  public StudentChoreographer(List<EventService> eventServices) {
    eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull Event event) {
    //only one thread will process all the request. since RDB wont handle concurrent requests.
    singleTaskExecutor.execute(() -> {
      try {
        switch (event.getEventType()) {
          case "CREATE_STUDENT":
            final StudentCreate studentCreate = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
            eventServiceMap.get(CREATE_STUDENT.toString()).processEvent(studentCreate, event);
            break;
          case "UPDATE_STUDENT":
            final StudentUpdate studentUpdate = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
            eventServiceMap.get(UPDATE_STUDENT.toString()).processEvent(studentUpdate, event);
            break;
          default:
            break;

        }
      } catch (final Exception exception) {
        log.error("Exception while processing event :: {}", event, exception);
      }
    });


  }
}
