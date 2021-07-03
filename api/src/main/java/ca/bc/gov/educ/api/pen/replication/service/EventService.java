package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;

/**
 * The interface Event service.
 *
 * @param <T> the type parameter
 */
public interface EventService<T> {

  /**
   * Process event.
   *
   * @param request the request
   * @param event   the event
   */
  void processEvent(T request, Event event);

  /**
   * Gets event type.
   *
   * @return the event type
   */
  String getEventType();
}
