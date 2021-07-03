package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.MatchAndTwinReasonCode;
import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

  private String buildInsert(final PossibleMatch possibleMatch) {
    final Map<String, Student> studentMap = this.createStudentMap(possibleMatch);
    return "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (" +
      "'" + studentMap.get(possibleMatch.getStudentID()).getPen() + "'" + "," +
      "'" + studentMap.get(possibleMatch.getMatchedStudentID()).getPen() + "'" + "," +
      "'" + this.findByPrrMatchCode(possibleMatch.getMatchReasonCode()).getOldCode() + "'" + "," +
      "'" + possibleMatch.getCreateDate().substring(0, 10).replace("-", "") + "'" + "," +
      "'" + possibleMatch.getCreateDate().substring(0, 10).replace("-", "") + "'" + "," +
      "'" + possibleMatch.getCreateUser() + "'" +
      ")";
  }


  private MatchAndTwinReasonCode findByPrrMatchCode(final MatchReasonCodes matchCode) {
    if (matchCode == null) {
      return MatchAndTwinReasonCode.AU;
    }
    return Arrays.stream(MatchAndTwinReasonCode.values()).filter(value -> value.getPrrCode().equals(matchCode.toString())).findFirst().orElse(MatchAndTwinReasonCode.AU);
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final List<PossibleMatch> possibleMatches) {
    for (final PossibleMatch possibleMatch : possibleMatches) {
      em.createNativeQuery(this.buildInsert(possibleMatch)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
    }
  }
}
