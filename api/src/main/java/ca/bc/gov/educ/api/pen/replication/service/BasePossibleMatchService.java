package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Base possible match service.
 */
public abstract class BasePossibleMatchService extends BaseService<List<PossibleMatch>> {
  /**
   * The Rest utils.
   */
  protected final RestUtils restUtils;
  private final PenTwinTransactionRepository penTwinTransactionRepository;

  /**
   * Instantiates a new Base service.
   *
   * @param eventRepository              the event repository
   * @param emf                          the emf
   * @param restUtils                    the rest utils
   * @param penTwinTransactionRepository the pen twin transaction repository
   */
  protected BasePossibleMatchService(final EventRepository eventRepository, final EntityManagerFactory emf, final RestUtils restUtils, final PenTwinTransactionRepository penTwinTransactionRepository) {
    super(eventRepository, emf);
    this.restUtils = restUtils;
    this.penTwinTransactionRepository = penTwinTransactionRepository;
  }

  /**
   * Check and process event.
   *
   * @param request the request
   * @param event   the event
   */
  protected void checkAndProcessEvent(final List<PossibleMatch> request, final Event event) {
    final List<PossibleMatch> possibleMatches = new ArrayList<>();
    for (val possibleMatch : request) {
      final Map<String, Student> studentMap = this.createStudentMap(possibleMatch);
      final String penTwin1 = studentMap.get(possibleMatch.getStudentID()).getPen();
      final String penTwin2 = studentMap.get(possibleMatch.getMatchedStudentID()).getPen();
      if (this.isEventPartOfOrchestratorSaga(this.penTwinTransactionRepository, penTwin1, penTwin2)) {
        continue;
      }
      possibleMatches.add(possibleMatch);
    }
    if (!possibleMatches.isEmpty()) {
      super.persistData(event, possibleMatches);
    } else {
      this.updateEvent(event);
    }
  }

  /**
   * Create student map map.
   *
   * @param possibleMatch the possible match
   * @return the map
   */
  protected Map<String, Student> createStudentMap(final PossibleMatch possibleMatch) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(possibleMatch.getStudentID());
    studentIDs.add(possibleMatch.getMatchedStudentID());
    return this.restUtils.getStudentsByID(studentIDs);
  }
}
