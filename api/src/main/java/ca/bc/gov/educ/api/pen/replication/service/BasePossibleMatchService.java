package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinsEntityID;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinsRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Base possible match service.
 */
@Slf4j
public abstract class BasePossibleMatchService extends BaseService<List<PossibleMatch>> {
  /**
   * The Rest utils.
   */
  protected final RestUtils restUtils;
  private final PenTwinTransactionRepository penTwinTransactionRepository;
  /**
   * The Pen twins repository.
   */
  protected final PenTwinsRepository penTwinsRepository;

  /**
   * Instantiates a new Base service.
   *
   * @param eventRepository              the event repository
   * @param emf                          the emf
   * @param restUtils                    the rest utils
   * @param penTwinTransactionRepository the pen twin transaction repository
   * @param penTwinsRepository           the pen twins repository
   */
  protected BasePossibleMatchService(final EventRepository eventRepository, final EntityManagerFactory emf, final RestUtils restUtils, final PenTwinTransactionRepository penTwinTransactionRepository, final PenTwinsRepository penTwinsRepository) {
    super(eventRepository, emf);
    this.restUtils = restUtils;
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.penTwinsRepository = penTwinsRepository;
  }

  /**
   * Check and process event.
   *
   * @param request   the request
   * @param event     the event
   * @param operation delete or create
   */
  protected void checkAndProcessEvent(final List<PossibleMatch> request, final Event event, final String operation) {
    final List<PossibleMatch> possibleMatches = new ArrayList<>();
    for (val possibleMatch : request) {
      final Map<String, Student> studentMap = this.createStudentMap(possibleMatch);
      final String penTwin1 = studentMap.get(possibleMatch.getStudentID()).getPen();
      final String penTwin2 = studentMap.get(possibleMatch.getMatchedStudentID()).getPen();
      if (this.isEventPartOfOrchestratorSaga(this.penTwinTransactionRepository, penTwin1, penTwin2) || this.dataAlreadyPresent(penTwin1, penTwin2, operation)) {
        continue;
      }
      possibleMatches.add(possibleMatch);
    }
    if (!possibleMatches.isEmpty()) {
      super.persistData(event, possibleMatches);
    } else {
      log.info("This event is part of orchestrator flow, marking it processed.");
      this.updateEvent(event);
    }
  }

  // this method will check if twins are already created or deleted.
  private boolean dataAlreadyPresent(final String penTwin1, final String penTwin2, final String operation) {
    val penTwinsLeftSideID = PenTwinsEntityID.builder()
      .penTwin1(penTwin1)
      .penTwin2(penTwin2)
      .build();
    val penTwinsEntityOptional = this.penTwinsRepository.findById(penTwinsLeftSideID);
    if (penTwinsEntityOptional.isPresent()) {
      return operation.equals("create"); // twin already exist  no need to do anything, for delete it will return false.
    } else {
      return operation.equals("delete"); // if it is not present and operation is delete, no need to do anything, for create it will add.
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
