package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.ADD_POSSIBLE_MATCH;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class PossibleMatchCreateService extends BaseService<PossibleMatch> {
  private final EntityManagerFactory emf;
  private final EventRepository eventRepository;

  @Autowired
  public PossibleMatchCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, final RestUtils restUtils) {
    super(restUtils);
    this.emf = emf;
    this.eventRepository = eventRepository;
  }

  @Override
  public void processEvent(final PossibleMatch request, final Event event) {
    final EntityManager em = this.emf.createEntityManager();
    final EntityTransaction tx = em.getTransaction();

    try {
      // below timeout is in milli seconds, so it is 10 seconds.
      tx.begin();
      em.createNativeQuery(this.buildInsert(request)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
      tx.commit();
      final var existingEvent = this.eventRepository.findByEventId(event.getEventId());
      existingEvent.ifPresent(record -> {
        record.setEventStatus(PROCESSED.toString());
        record.setUpdateDate(LocalDateTime.now());
        this.eventRepository.save(record);
      });
    } catch (final Exception e) {
      log.error("Error occurred saving entity " + e.getMessage());
      tx.rollback();
    } finally {
      if (em.isOpen()) {
        em.close();
      }
    }
  }


  @Override
  public String getEventType() {
    return ADD_POSSIBLE_MATCH.toString();
  }

  private String buildInsert(final PossibleMatch possibleMatch) {
    return "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (" +
        "'" + this.getStudentPen(possibleMatch.getStudentID()) + "'" + "," +
        "'" + this.getStudentPen(possibleMatch.getMatchedStudentID()) + "'" + "," +
        "'" + MatchReasonCodes.AU.toString() + "'" + "," +
        "TO_DATE('" + possibleMatch.getCreateDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS')," +
        "TO_DATE('" + possibleMatch.getCreateDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS')," +
        "'" + possibleMatch.getCreateUser() + "'" +
        ")";
  }
}
