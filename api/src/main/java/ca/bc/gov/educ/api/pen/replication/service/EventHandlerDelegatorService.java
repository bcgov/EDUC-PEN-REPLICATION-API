package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.choreographer.StudentChoreographer;
import ca.bc.gov.educ.api.pen.replication.exception.BusinessException;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import io.nats.streaming.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * This class is responsible to process events from STAN.
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {

  private final ChoreographedEventPersistenceService choreographedEventPersistenceService;
  private final StudentChoreographer studentChoreographer;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param choreographedEventPersistenceService the choreographed event persistence service
   * @param studentChoreographer
   */
  @Autowired
  public EventHandlerDelegatorService(ChoreographedEventPersistenceService choreographedEventPersistenceService, StudentChoreographer studentChoreographer) {
    this.choreographedEventPersistenceService = choreographedEventPersistenceService;
    this.studentChoreographer = studentChoreographer;
  }

  /**
   * this method will do the following.
   * 1. Call service to store the event in oracle DB.
   * 2. Acknowledge to STAN only when the service call is completed. since it uses manual acknowledgement.
   * 3. Hand off the task to update RDB onto a different executor.
   *
   * @param choreographedEvent the choreographed event
   * @param message            the message
   * @throws IOException the io exception
   */
  public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) throws IOException {
    try {
      var persistedEvent = choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
      message.ack(); // acknowledge to STAN that api got the message and it is now in DB.
      studentChoreographer.handleEvent(persistedEvent);
    } catch (final BusinessException businessException) {
      log.info("this event is already persisted. :: {}", choreographedEvent, businessException);
      message.ack(); // acknowledge to STAN that api got the message
    }
  }
}
