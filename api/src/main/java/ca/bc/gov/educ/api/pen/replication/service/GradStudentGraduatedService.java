package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GradStatusEventPayloadDTO;
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
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.GRAD_STUDENT_GRADUATED;

@Service
@Slf4j
public class GradStudentGraduatedService extends BaseService<GradStatusEventPayloadDTO> {

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
  public void processEvent(final GradStatusEventPayloadDTO gradStatusEventPayloadDTO, final Event event) {
    var studentPEN = gradStatusEventPayloadDTO.getPen();
    List<GradStudentRecordCoursesLoad> courseListFromGradStudentClob = this.getStudentCourses(studentPEN, event.getEventId());
    log.debug("course list from grad student clob for pen {} :: response is {}", studentPEN, courseListFromGradStudentClob);
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
    if (existingTraxStudentRecord.isPresent()) {
      var existingCourses = this.traxStudentCourseService.findTraxStudentCoursesByPen(StringUtils.rightPad(studentPEN, 10));
      existingCourses = existingCourses.stream().filter(Objects::nonNull).toList();
      this.traxStudentCourseService.deletePriorAndSaveTraxStudentCourses(existingCourses, getStudentCourseEntityList(studentPEN, courseListFromGradStudentClob));
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), gradStatusEventPayloadDTO);
    } else {
      log.info("Student course event not processed {} :: payload is: {} :: student does not yet exist in TRAX STUDENT_MASTER", event.getEventId(), gradStatusEventPayloadDTO);
      throw new PenReplicationAPIRuntimeException("GradStudentGraduatedService :: Student course event not processed");
    }
    this.updateEvent(event);
  }

  private List<GradStudentRecordCoursesLoad> getStudentCourses(String pen, UUID eventId) {
    try {
      return this.restUtils.getGradStudentCourseListFromClobByPen(pen);
    } catch (RuntimeException e) {
      log.error("Failed to retrieve GRAD student courses for PEN {} while processing graduation event {} this should not happen", pen, eventId, e);
      throw e;
    }
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
  protected void buildAndExecutePreparedStatements(final EntityManager em, final GradStatusEventPayloadDTO gradStatusEventPayloadDTO) {
    // Not required this child class use repository pattern of spring.
  }
}
