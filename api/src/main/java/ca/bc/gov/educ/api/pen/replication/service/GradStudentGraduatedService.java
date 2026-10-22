package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
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

import java.util.*;

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
      val existingTraxStudent = populateExistingTraxStudentEntity(existingTraxStudentRecord.get(), gradStatusEventPayloadDTO);
      this.traxStudentService.saveTraxStudentInCurrentTransaction(existingTraxStudent);
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
    studentCourse.forEach(course -> {
      TraxStudentCourseEntity traxStudentCourseEntity = new TraxStudentCourseEntity();
      traxStudentCourseEntity.setCourseCode(StringUtils.trimToNull(course.getCourseCode()));
      traxStudentCourseEntity.setCourseLevel(StringUtils.trimToNull(course.getCourseLevel()));
      traxStudentCourseEntity.setStudNo(StringUtils.trimToNull(studentPEN));
      traxStudentCourseEntity.setCourseSession(StringUtils.trimToNull(StringUtils.remove(course.getSessionDate(), "/")));
      traxStudentCourseEntity.setFinalLetterGrade(StringUtils.trimToNull(course.getFinalLetterGrade()));
      traxStudentCourseEntity.setFinalPercentage(course.getFinalPercent() != null ? course.getFinalPercent().toString() : null);
      traxStudentCourseEntity.setNumberOfCredits(course.getCredits() != null ? course.getCredits().toString() : null);
      traxStudentCourseEntity.setUsedForGrad(StringUtils.trimToNull(course.getCreditsUsedForGrad()));
      traxStudentCourseEntity.setStudyType(determineStudyType(course.getGradReqMet()));
      entityList.add(traxStudentCourseEntity);
    });
    return entityList;
  }

  private TraxStudentEntity populateExistingTraxStudentEntity(TraxStudentEntity existingTraxStudent, GradStatusEventPayloadDTO gradStatusEventPayloadDTO) {
    var schoolOfRecord = this.restUtils.getSchoolBySchoolID(gradStatusEventPayloadDTO.getSchoolOfRecordId().toString()).get();
    var gradProgramCode = StringUtils.isNotBlank(gradStatusEventPayloadDTO.getProgram()) ? gradStatusEventPayloadDTO.getProgram().substring(0, 4) : null;

    String mincodeGrad = null;
    String gradDate;
    if (gradProgramCode != null && gradProgramCode.equalsIgnoreCase("SCCP")) {
      mincodeGrad = null;
      gradDate = null;
    } else {
      if (gradStatusEventPayloadDTO.getSchoolAtGradId() != null) {
        mincodeGrad = restUtils.getSchoolBySchoolID(gradStatusEventPayloadDTO.getSchoolAtGradId().toString()).get().getMincode();
      }
      gradDate = StringUtils.isNotBlank(gradStatusEventPayloadDTO.getProgramCompletionDate()) ? gradStatusEventPayloadDTO.getProgramCompletionDate().substring(0, 7).replace("-", "") : null;
    }

    existingTraxStudent.setMincode(schoolOfRecord.getMincode());
    existingTraxStudent.setMincodeGrad(mincodeGrad);
    existingTraxStudent.setGradDate(StringUtils.isNotBlank(gradDate) ? Long.valueOf(gradDate) : null);
    existingTraxStudent.setGradReqtYear(gradProgramCode);

    return existingTraxStudent;
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

  /**
   * Determines the TRAX study type from comma-separated graduation requirement codes.
   * <p>
   * A requirement code of "3" maps to " 3", including the leading space required
   * by TRAX. All other codes are ignored. Whitespace around each code is trimmed
   * before comparison.
   *
   * @param gradReqMet graduation requirement codes; may be null, empty, or blank
   * @return " 3" if an exact code of "3" is present; otherwise null
   */
  public String determineStudyType(String gradReqMet) {
    if (StringUtils.isBlank(gradReqMet)) {
      return null;
    }

    boolean hasRequirementThree = Arrays.stream(gradReqMet.split(","))
        .anyMatch(requirement -> "3".equals(requirement.trim()));

    return hasRequirementThree ? " 3" : null;
  }
}
