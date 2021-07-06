package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.ADD_POSSIBLE_MATCH;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class PossibleMatchCreateService extends BasePossibleMatchService {

  /**
   * The Rest utils.
   */

  /**
   * Instantiates a new Possible match create service.
   *
   * @param emf                          the emf
   * @param eventRepository              the event repository
   * @param penTwinTransactionRepository the pen twin transaction repository
   * @param restUtils                    the rest utils
   */
  @Autowired
  public PossibleMatchCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, final PenTwinTransactionRepository penTwinTransactionRepository, final RestUtils restUtils) {
    super(eventRepository, emf, restUtils, penTwinTransactionRepository);
  }

  @Override
  public void processEvent(final List<PossibleMatch> request, final Event event) {
    this.checkAndProcessEvent(request, event);
  }


  @Override
  public String getEventType() {
    return ADD_POSSIBLE_MATCH.toString();
  }


  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final List<PossibleMatch> possibleMatches) {
    for (final PossibleMatch possibleMatch : possibleMatches) {
      em.createNativeQuery(PenReplicationHelper.buildPenTwinInsert(possibleMatch, this.restUtils)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
    }
  }
}
