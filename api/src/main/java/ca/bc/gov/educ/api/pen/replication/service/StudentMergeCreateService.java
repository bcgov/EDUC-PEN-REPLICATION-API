package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_MERGE;

/**
 * This class is responsible to add possible matches
 */
@Service
@Slf4j
public class StudentMergeCreateService extends BaseService<List<StudentMerge>> {
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  /**
   * Instantiates a new Student merge create service.
   *
   * @param emf             the emf
   * @param eventRepository the event repository
   * @param restUtils       the rest utils
   */
  @Autowired
  public StudentMergeCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, final RestUtils restUtils) {
    super(eventRepository, emf);
    this.restUtils = restUtils;
  }

  @Override
  public void processEvent(final List<StudentMerge> studentMergeList, final Event event) {
    super.persistData(event, studentMergeList);
  }


  @Override
  public String getEventType() {
    return CREATE_MERGE.toString();
  }

  private String buildInsert(final StudentMerge studentMerge) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(studentMerge.getStudentID());
    studentIDs.add(studentMerge.getMergeStudentID());
    val studentMap = this.restUtils.getStudentsByID(studentIDs);
    return "insert into pen_merges (STUD_NO, STUD_TRUE_NO) values (" +
      "'" + studentMap.get(studentMerge.getStudentID()).getPen() + "'" + "," +
      "'" + studentMap.get(studentMerge.getMergeStudentID()).getPen() + "'" +
      ")";
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final List<StudentMerge> studentMerges) {
    for (final StudentMerge studentMerge : studentMerges) {
      em.createNativeQuery(this.buildInsert(studentMerge)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
    }
  }
}
