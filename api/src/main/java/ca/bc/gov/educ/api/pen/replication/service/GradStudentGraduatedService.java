package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.model.dc.GradStudentCoursePayload;
import ca.bc.gov.educ.api.pen.replication.model.dc.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GraduationStudentRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.GRAD_STUDENT_GRADUATED;

@Service
@Slf4j
public class GradStudentGraduatedService extends BaseService<GraduationStudentRecord> {

  private final TraxStudentService traxStudentService;
  private final TraxStudentCourseService traxStudentCourseService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  public GradStudentGraduatedService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, TraxStudentCourseService traxStudentCourseService, final RestUtils restUtils) {
    super(eventRepository, emf);
    this.traxStudentService = traxStudentService;
    this.traxStudentCourseService = traxStudentCourseService;
    this.restUtils = restUtils;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processEvent(final GraduationStudentRecord graduationStudentRecord, final Event event) {
    var studentPEN = restUtils.getStudentPen(graduationStudentRecord.getStudentID().toString());
    List<GradStudentRecordCoursesLoad> courseListFromClob = parseStudentGradData(graduationStudentRecord.getStudentGradData()).getStudentCourses().getStudentCourseList();
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
    if (existingTraxStudentRecord.isPresent()) {
      var existingCourses = this.traxStudentCourseService.findTraxStudentCoursesByPen(StringUtils.rightPad(studentPEN, 10));
      existingCourses = existingCourses.stream().filter(Objects::nonNull).toList();
      this.traxStudentCourseService.deletePriorAndSaveTraxStudentCourses(existingCourses, getStudentCourseEntityList(studentPEN, courseListFromClob));
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), graduationStudentRecord);
    } else {
      log.info("Student course event not processed {} :: payload is: {} :: student does not yet exist in TRAX STUDENT_MASTER", event.getEventId(), graduationStudentRecord);
      throw new PenReplicationAPIRuntimeException("GradStudentGraduatedService :: Student course event not processed");
    }
    this.updateEvent(event);
  }

  private List<TraxStudentCourseEntity> getStudentCourseEntityList(String studentPEN, List<GradStudentRecordCoursesLoad> studentCourse) {
    var entityList = new ArrayList<TraxStudentCourseEntity>();
    studentCourse.forEach(student -> {
      TraxStudentCourseEntity traxStudentCourseEntity = new TraxStudentCourseEntity();
      traxStudentCourseEntity.setCourseCode(StringUtils.trimToNull(student.getCourseCode()));
      traxStudentCourseEntity.setCourseLevel(StringUtils.trimToNull(student.getCourseLevel()));
      traxStudentCourseEntity.setStudNo(StringUtils.trimToNull(studentPEN));
      traxStudentCourseEntity.setCourseSession(StringUtils.trimToNull(StringUtils.remove(student.getSessionDate(), "/")));
      traxStudentCourseEntity.setFinalLetterGrade(StringUtils.trimToNull(student.getFinalLetterGrade()));
      traxStudentCourseEntity.setFinalPercentage(student.getFinalPercent() != null ? student.getFinalPercent() : null);
      traxStudentCourseEntity.setNumberOfCredits(student.getCredits() != null ? student.getCredits() : null);
      entityList.add(traxStudentCourseEntity);
    });
    return entityList;
  }

  /**
   * Gets event type.
   *
   * @return the event type
   */
  @Override
  public String getEventType() {
    return GRAD_STUDENT_GRADUATED.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final GraduationStudentRecord graduationStudentRecord) {
    // Not required this child class use repository pattern of spring.
  }

  public GradStudentCoursePayload parseStudentGradData(String studentGradData) {
    GradStudentCoursePayload gradStudentCoursePayload = null;
    try {
      gradStudentCoursePayload = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(studentGradData, GradStudentCoursePayload.class);
    } catch (Exception e) {
      log.debug("parseStudentGrad Data Error {}", e.getMessage());
    }
    return gradStudentCoursePayload;
  }
}
