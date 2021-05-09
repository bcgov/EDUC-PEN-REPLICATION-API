package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.DELETE_MERGE;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class StudentMergeDeleteService extends BaseService {
  private final EntityManagerFactory emf;
  private final EventRepository eventRepository;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  @Autowired
  public StudentMergeDeleteService(EntityManagerFactory emf, EventRepository eventRepository, RestUtils restUtils) {
    this.emf = emf;
    this.eventRepository = eventRepository;
    this.restUtils = restUtils;
  }

  @Override
  public <T extends Object> void processEvent(T request, Event event) {
    List<StudentMerge> studentMergeList = (List<StudentMerge>) request;

    EntityManager em = this.emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
      // below timeout is in milli seconds, so it is 10 seconds.
      tx.begin();
      for(StudentMerge studentMerge: studentMergeList) {
        em.createNativeQuery(buildInsert(studentMerge)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
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



  @Override
  public String getEventType() {
    return DELETE_MERGE.toString();
  }

  private String buildInsert(StudentMerge studentMerge) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(studentMerge.getStudentID());
    studentIDs.add(studentMerge.getMergeStudentID());
    val studentMap = restUtils.getStudentsByID(studentIDs);
    return "delete from pen_merges where STUD_NO = '"
            + studentMap.get(studentMerge.getStudentID()).getPen() + "'" +
            " AND STUD_TRUE_NO = '" + studentMap.get(studentMerge.getMergeStudentID()).getPen() + "'";
  }
}
