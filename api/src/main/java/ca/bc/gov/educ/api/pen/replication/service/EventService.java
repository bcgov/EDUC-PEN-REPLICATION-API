package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;

public interface EventService {

  <T extends BaseRequest> void processEvent(T request, Event event);

  String getEventType();
}
