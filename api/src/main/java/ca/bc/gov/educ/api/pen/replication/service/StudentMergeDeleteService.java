package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.DELETE_MERGE;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class StudentMergeDeleteService extends BaseService<StudentMerge> {
  private final EntityManagerFactory emf;
  private final EventRepository eventRepository;

  @Autowired
  public StudentMergeDeleteService(final EntityManagerFactory emf, final EventRepository eventRepository, final RestUtils restUtils) {
    super(restUtils);
    this.emf = emf;
    this.eventRepository = eventRepository;
  }

  @Override
  public void processEvent(final StudentMerge request, final Event event) {

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
    return DELETE_MERGE.toString();
  }

  private String buildInsert(final StudentMerge studentMerge) {
    return "delete from pen_merges where STUD_NO = '"
        + this.getStudentPen(studentMerge.getStudentID()) + "'" +
        " AND STUD_TRUE_NO = '" + this.getStudentPen(studentMerge.getMergeStudentID()) + "'";
  }
}
