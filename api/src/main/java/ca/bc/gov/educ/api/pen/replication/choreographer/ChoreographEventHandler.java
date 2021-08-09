package ca.bc.gov.educ.api.pen.replication.choreographer;

import ca.bc.gov.educ.api.pen.replication.constants.EventStatus;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.service.EventService;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;


/**
 * The type Choreograph event handler.
 */
@Component
@Slf4j
public class ChoreographEventHandler {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(2).build();
  private final Map<String, EventService<?>> eventServiceMap;
  private final EventRepository eventRepository;

  /**
   * Instantiates a new Choreograph event handler.
   *
   * @param eventServices   the event services
   * @param eventRepository the event repository
   */
  public ChoreographEventHandler(final List<EventService<?>> eventServices, final EventRepository eventRepository) {
    this.eventRepository = eventRepository;
    this.eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  /**
   * Handle event.
   *
   * @param event the event
   */
  public void handleEvent(@NonNull final Event event) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {
      val eventFromDBOptional = this.eventRepository.findById(event.getPenReplicationEventId());
      if (eventFromDBOptional.isPresent()) {
        val eventFromDB = eventFromDBOptional.get();
        if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
          log.info("Processing event with replication event ID :: {}", event.getPenReplicationEventId());
          try {
            switch (event.getEventType()) {
              case "CREATE_STUDENT":
                val studentCreate = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
                final EventService<StudentCreate> studentCreateEventService = (EventService<StudentCreate>) this.eventServiceMap.get(CREATE_STUDENT.toString());
                studentCreateEventService.processEvent(studentCreate, event);
                break;
              case "UPDATE_STUDENT":
                log.info("Processing UPDATE_STUDENT event record :: {} ", event);
                val studentUpdate = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
                final EventService<StudentUpdate> studentUpdateEventService = (EventService<StudentUpdate>) this.eventServiceMap.get(UPDATE_STUDENT.toString());
                studentUpdateEventService.processEvent(studentUpdate, event);
                break;
              case "ADD_POSSIBLE_MATCH":
                final List<PossibleMatch> possibleMatchList = JsonUtil.objectMapper.readValue(event.getEventPayload(), new TypeReference<>() {
                });
                final EventService<List<PossibleMatch>> addPossibleMatchService = (EventService<List<PossibleMatch>>) this.eventServiceMap.get(ADD_POSSIBLE_MATCH.toString());
                addPossibleMatchService.processEvent(possibleMatchList, event);
                break;
              case "DELETE_POSSIBLE_MATCH":

                final List<PossibleMatch> deletePossibleMatchList = JsonUtil.objectMapper.readValue(event.getEventPayload(), new TypeReference<>() {
                });
                final EventService<List<PossibleMatch>> deletePossibleMatchService = (EventService<List<PossibleMatch>>) this.eventServiceMap.get(DELETE_POSSIBLE_MATCH.toString());
                deletePossibleMatchService.processEvent(deletePossibleMatchList, event);
                break;
              case "CREATE_MERGE":
                final List<StudentMerge> createStudentMergeList = JsonUtil.objectMapper.readValue(event.getEventPayload(), new TypeReference<>() {
                });
                final EventService<List<StudentMerge>> createMergeService = (EventService<List<StudentMerge>>) this.eventServiceMap.get(CREATE_MERGE.toString());
                createMergeService.processEvent(createStudentMergeList, event);
                break;
              case "DELETE_MERGE":
                final List<StudentMerge> deleteStudentMergeList = JsonUtil.objectMapper.readValue(event.getEventPayload(), new TypeReference<>() {
                });
                final EventService<List<StudentMerge>> deleteMergeService = (EventService<List<StudentMerge>>) this.eventServiceMap.get(DELETE_MERGE.toString());
                deleteMergeService.processEvent(deleteStudentMergeList, event);
                break;
              default:
                log.warn("Silently ignoring event: {}", event);
                break;

            }
            log.info("Replication Event was processed, ID :: {}", event.getPenReplicationEventId());
          } catch (final Exception exception) {
            log.error("Exception while processing event :: {}", event, exception);
          }
        }
      }

    });


  }
}
