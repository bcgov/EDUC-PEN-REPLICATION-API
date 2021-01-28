package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.BusinessError;
import ca.bc.gov.educ.api.pen.replication.exception.BusinessException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;


@Service
@Slf4j
public class ChoreographedEventPersistenceService {
  private final EventRepository eventRepository;

  @Autowired
  public ChoreographedEventPersistenceService(final EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Event persistEventToDB(final ChoreographedEvent choreographedEvent) throws BusinessException {
    var eventOptional = eventRepository.findByEventId(choreographedEvent.getEventID());
    if (eventOptional.isPresent()) {
      throw new BusinessException(BusinessError.EVENT_ALREADY_PERSISTED, choreographedEvent.getEventID().toString());
    }
    final Event event = Event.builder()
        .eventType(choreographedEvent.getEventType().toString())
        .eventId(choreographedEvent.getEventID())
        .eventOutcome(choreographedEvent.getEventOutcome().toString())
        .eventPayload(choreographedEvent.getEventPayload())
        .eventStatus(DB_COMMITTED.toString())
        .createUser(StringUtils.isBlank(choreographedEvent.getCreateUser()) ? "PEN-REPLICATION-API" : choreographedEvent.getCreateUser())
        .updateUser(StringUtils.isBlank(choreographedEvent.getUpdateUser()) ? "PEN-REPLICATION-API" : choreographedEvent.getUpdateUser())
        .build();
    return eventRepository.save(event);
  }
}
