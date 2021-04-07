package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.ADD_POSSIBLE_MATCH;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class PossibleMatchCreateService extends BaseService {
  private final EntityManagerFactory emf;
  private final EventRepository eventRepository;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  @Autowired
  public PossibleMatchCreateService(EntityManagerFactory emf, EventRepository eventRepository, RestUtils restUtils) {
    this.emf = emf;
    this.eventRepository = eventRepository;
    this.restUtils = restUtils;
  }

  @Override
  public <T extends Object> void processEvent(T request, Event event) {
    List<PossibleMatch> possibleMatchList = (List<PossibleMatch>) request;

    EntityManager em = this.emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
      // below timeout is in milli seconds, so it is 10 seconds.
      tx.begin();
      for(PossibleMatch possibleMatch: possibleMatchList) {
        em.createNativeQuery(buildInsert(possibleMatch)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
      }
      tx.commit();
      var existingEvent = eventRepository.findByEventId(event.getEventId());
      existingEvent.ifPresent(record -> {
        record.setEventStatus(PROCESSED.toString());
        record.setUpdateDate(LocalDateTime.now());
        eventRepository.save(record);
      });
    } catch (Exception e) {
      log.error("Error occurred saving entity " + e.getMessage());
      tx.rollback();
    } finally {
      if (em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Gets student true pen number.
   *
   * @param studentID the true student id
   * @return the student true number
   */
  private String getStudentPen(String studentID) {
    return restUtils.getStudentPen(studentID).orElseThrow();
  }


  @Override
  public String getEventType() {
    return ADD_POSSIBLE_MATCH.toString();
  }

  private String buildInsert(PossibleMatch possibleMatch) {
    String insert = "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (" +
        "'" + getStudentPen(possibleMatch.getStudentID()) + "'" + "," +
        "'" + getStudentPen(possibleMatch.getMatchedStudentID()) + "'" + "," +
        "'" + MatchReasonCodes.AU.toString() + "'" + "," +
        "'" + possibleMatch.getCreateDate().substring(0,8) + "'" + "," +
        "'" + possibleMatch.getCreateDate().substring(0,8) + "'" + "," +
        "'" + possibleMatch.getCreateUser() + "'" +
        ")";
    log.info("TWINS insert: " + insert);
    return insert;
  }
}