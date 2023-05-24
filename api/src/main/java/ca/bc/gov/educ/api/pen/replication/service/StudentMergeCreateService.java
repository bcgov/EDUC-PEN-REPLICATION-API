package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenMergePK;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenMergeRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.struct.StudentMerge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_MERGE;

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
  private final PenMergeRepository penMergeRepository;

  /**
   * Instantiates a new Student merge create service.
   *
   * @param emf                the emf
   * @param eventRepository    the event repository
   * @param restUtils          the rest utils
   * @param penMergeRepository the pen merge repository
   */
  @Autowired
  public StudentMergeCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, final RestUtils restUtils, PenMergeRepository penMergeRepository) {
    super(eventRepository, emf);
    this.restUtils = restUtils;
    this.penMergeRepository = penMergeRepository;
  }

  /**
   * * MergedPEN is merged "TO" TruePEN
   * *      * StudentMerge entity
   * *      *    studentID       : MergedPEN's studentID   (merged student)
   * *      *    directionCode   : "TO"
   * *      *    mergeStudentID  : TruePEN's studentID     (active student)
   * *      *
   *
   * @param studentMergeList the list of merges. only interested in merge to.
   * @param event            the event
   */
  @Override
  public void processEvent(final List<StudentMerge> studentMergeList, final Event event) {
    final List<StudentMerge> studentMerges = new ArrayList<>();
    for (val merge : studentMergeList) {
      if ("TO".equalsIgnoreCase(merge.getStudentMergeDirectionCode())) {
        final Map<String, Student> studentMap = getStudentMapFromMerge(merge);
        PenMergePK penMergePK = new PenMergePK(studentMap.get(merge.getStudentID()).getPen(), studentMap.get(merge.getMergeStudentID()).getPen());
        val penMerge = this.penMergeRepository.findById(penMergePK);
        if (penMerge.isEmpty()) {
          studentMerges.add(merge);
        }
      }
    }
    if (!studentMerges.isEmpty()) {
      super.persistData(event, studentMerges);
    } else {
      log.info("The merge is already present, ignoring.");
      this.updateEvent(event);
    }
  }


  @Override
  public String getEventType() {
    return CREATE_MERGE.toString();
  }

  private String buildInsert(final StudentMerge studentMerge) {
    final Map<String, Student> studentMap = getStudentMapFromMerge(studentMerge);
    return "insert into pen_merges (STUD_NO, STUD_TRUE_NO) values (" +
      "'" + studentMap.get(studentMerge.getStudentID()).getPen() + "'" + "," +
      "'" + studentMap.get(studentMerge.getMergeStudentID()).getPen() + "'" +
      ")";
  }

  private Map<String, Student> getStudentMapFromMerge(StudentMerge studentMerge) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(studentMerge.getStudentID());
    studentIDs.add(studentMerge.getMergeStudentID());
    return this.restUtils.getStudentsByID(studentIDs);
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final List<StudentMerge> studentMerges) {
    for (final StudentMerge studentMerge : studentMerges) {
      em.createNativeQuery(this.buildInsert(studentMerge)).setHint("jakarta.persistence.query.timeout", 10000).executeUpdate();
    }
  }
}
