package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.BusinessError;
import ca.bc.gov.educ.api.pen.replication.exception.BusinessException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_AUTHORITY;


/**
 * The type Choreographed event persistence service.
 */
@Service
@Slf4j
public class ChoreographedEventPersistenceService {
  private final EventRepository eventRepository;

  /**
   * Instantiates a new Choreographed event persistence service.
   *
   * @param eventRepository the event repository
   */
  @Autowired
  public ChoreographedEventPersistenceService(final EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Persist event to db event.
   *
   * @param choreographedEvent the choreographed event
   * @return the event
   * @throws BusinessException the business exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Event persistEventToDB(final ChoreographedEvent choreographedEvent) throws BusinessException {
    final var eventOptional = this.eventRepository.findByEventId(choreographedEvent.getEventID());
    if (eventOptional.isPresent()) {
      throw new BusinessException(BusinessError.EVENT_ALREADY_PERSISTED, choreographedEvent.getEventID().toString());
    }
    val event = Event.builder()
      .eventType(choreographedEvent.getEventType().toString())
      .eventId(choreographedEvent.getEventID())
      .eventOutcome(choreographedEvent.getEventOutcome().toString())
      .eventPayload(choreographedEvent.getEventPayload())
      .eventStatus(DB_COMMITTED.toString())
      .createUser(StringUtils.isBlank(choreographedEvent.getCreateUser()) ? "PEN-REPLICATION-API" : choreographedEvent.getCreateUser())
      .updateUser(StringUtils.isBlank(choreographedEvent.getUpdateUser()) ? "PEN-REPLICATION-API" : choreographedEvent.getUpdateUser())
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
    return this.eventRepository.save(event);
  }
}
