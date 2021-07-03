package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.DELETE_POSSIBLE_MATCH;

/**
 * This class is responsible to delete possible matches
 */
@Service
@Slf4j
public class PossibleMatchDeleteService extends BasePossibleMatchService {


  /**
   * Instantiates a new Possible match delete service.
   *
   * @param emf                          the emf
   * @param eventRepository              the event repository
   * @param restUtils                    the rest utils
   * @param penTwinTransactionRepository the pen twin transaction repository
   */
  @Autowired
  public PossibleMatchDeleteService(final EntityManagerFactory emf, final EventRepository eventRepository, final RestUtils restUtils, final PenTwinTransactionRepository penTwinTransactionRepository) {
    super(eventRepository, emf, restUtils, penTwinTransactionRepository);
  }

  @Override
  public void processEvent(final List<PossibleMatch> request, final Event event) {
    this.checkAndProcessEvent(request, event);
  }


  @Override
  public String getEventType() {
    return DELETE_POSSIBLE_MATCH.toString();
  }

  private String buildDelete(final PossibleMatch possibleMatch) {
    val studentMap = this.createStudentMap(possibleMatch);
    return "delete from pen_twins where PEN_TWIN1 = '"
      + studentMap.get(possibleMatch.getStudentID()).getPen() + "'" +
      " AND PEN_TWIN2 = '" + studentMap.get(possibleMatch.getMatchedStudentID()).getPen() + "'";
  }


  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final List<PossibleMatch> possibleMatches) {
    for (final PossibleMatch possibleMatch : possibleMatches) {
      em.createNativeQuery(this.buildDelete(possibleMatch)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
    }
  }
}
