package ca.bc.gov.educ.api.pen.replication.choreographer;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.service.EventService;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.*;


/**
 * This class is responsible to handle different choreographed events related student by calling different services.
 */

@Component
@Slf4j
public class ChoreographEventHandler {
  private final ObjectMapper mapper = new ObjectMapper();
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
      .setCorePoolSize(1).setMaximumPoolSize(1).build();
  private final Map<String, EventService> eventServiceMap;

  public ChoreographEventHandler(final List<EventService> eventServices) {
    this.eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull final Event event) {
    //only one thread will process all the request. since RDB wont handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {

      try {
        switch (event.getEventType()) {


          case "CREATE_STUDENT":
            final StudentCreate studentCreate = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
            this.eventServiceMap.get(CREATE_STUDENT.toString()).processEvent(studentCreate, event);
            break;
          case "UPDATE_STUDENT":
            log.info("Processing UPDATE_STUDENT event record :: {} ", event);
            final StudentUpdate studentUpdate = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
            this.eventServiceMap.get(UPDATE_STUDENT.toString()).processEvent(studentUpdate, event);
            break;
          case "ADD_POSSIBLE_MATCH":
            final List<PossibleMatch> possibleMatchList = this.mapper.readValue(event.getEventPayload(), new TypeReference<>() {
            });
            this.eventServiceMap.get(ADD_POSSIBLE_MATCH.toString()).processEvent(possibleMatchList, event);
            break;
          case "DELETE_POSSIBLE_MATCH":
            final List<PossibleMatch> deletePossibleMatchList = this.mapper.readValue(event.getEventPayload(), new TypeReference<>() {
            });
            this.eventServiceMap.get(DELETE_POSSIBLE_MATCH.toString()).processEvent(deletePossibleMatchList, event);
            break;
          case "CREATE_MERGE":
            final List<StudentMerge> createStudentMergeList = this.mapper.readValue(event.getEventPayload(), new TypeReference<>() {
            });
            this.eventServiceMap.get(CREATE_MERGE.toString()).processEvent(createStudentMergeList, event);
            break;
          case "DELETE_MERGE":
            final List<StudentMerge> deleteStudentMergeList = this.mapper.readValue(event.getEventPayload(), new TypeReference<>() {
            });
            this.eventServiceMap.get(DELETE_MERGE.toString()).processEvent(deleteStudentMergeList, event);
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