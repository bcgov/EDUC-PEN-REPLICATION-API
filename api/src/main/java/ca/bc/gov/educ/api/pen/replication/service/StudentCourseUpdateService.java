package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.StudXcrseId;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCourse;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCourseUpdate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_STUDENT_COURSES;

@Service
@Slf4j
public class StudentCourseUpdateService extends BaseService<StudentCourseUpdate> {

  private final TraxStudentService traxStudentService;
  private final TraxStudentCourseService traxStudentCourseService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  public StudentCourseUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, TraxStudentCourseService traxStudentCourseService, final RestUtils restUtils) {
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
  public void processEvent(final StudentCourseUpdate studentCourseUpdate, final Event event) {
    var studentPEN = restUtils.getStudentPen(studentCourseUpdate.getStudentID());
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
    if (existingTraxStudentRecord.isPresent()) {
      var existingCourses = this.traxStudentCourseService.findTraxStudentCoursesByPen(studentPEN);
      this.traxStudentCourseService.deletePriorAndSaveTraxStudentCourses(studentPEN, getStudentCourseEntityList(studentPEN, studentCourseUpdate.getStudentCourses(), existingCourses));
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), studentCourseUpdate);
    }else{
      log.info("Student course event not processed {} :: payload is: {} :: student does not yet exist in TRAX STUDENT_MASTER", event.getEventId(), studentCourseUpdate);
      throw new PenReplicationAPIRuntimeException("Student course event not processed");
    }
    this.updateEvent(event);
  }

  private List<TraxStudentCourseEntity> getStudentCourseEntityList(String studentPEN, List<StudentCourse> studentCourse, List<TraxStudentCourseEntity> existingTraxStudentCourses) {
    var entityList = new ArrayList<TraxStudentCourseEntity>();
    studentCourse.forEach(student -> {
      TraxStudentCourseEntity traxStudentCourseEntity = new TraxStudentCourseEntity();
      traxStudentCourseEntity.setStudXcrseId(new StudXcrseId());
      setCourseCodeAndLevel(traxStudentCourseEntity, student.getCourseID());
      traxStudentCourseEntity.getStudXcrseId().setStudNo(studentPEN);
      traxStudentCourseEntity.getStudXcrseId().setCourseSession(student.getCourseSession());
      traxStudentCourseEntity.setFinalLetterGrade(student.getFinalLetterGrade());
      traxStudentCourseEntity.setFinalPercentage(student.getFinalPercent() != null ? student.getFinalPercent().toString() : null);
      traxStudentCourseEntity.setNumberOfCredits(student.getCredits() != null ? student.getCredits().toString() : null);
      setStudyTypeAndUsedForGradFields(traxStudentCourseEntity, existingTraxStudentCourses);
      entityList.add(traxStudentCourseEntity);
    });
    return entityList;
  }
  
  private void setStudyTypeAndUsedForGradFields(TraxStudentCourseEntity traxStudentCourseEntity, List<TraxStudentCourseEntity> existingTraxStudentCourses){
    for(TraxStudentCourseEntity course: existingTraxStudentCourses){
      if(course.getStudXcrseId().getCourseCode().equals(traxStudentCourseEntity.getStudXcrseId().getCourseCode()) &&
          course.getStudXcrseId().getCourseLevel().equals(traxStudentCourseEntity.getStudXcrseId().getCourseLevel()) &&
          course.getStudXcrseId().getCourseSession().equals(traxStudentCourseEntity.getStudXcrseId().getCourseSession())){
        traxStudentCourseEntity.setStudyType(course.getStudyType());
        traxStudentCourseEntity.setUsedForGrad(course.getUsedForGrad());
        break;
      }
    }
  }
  
  private void setCourseCodeAndLevel(TraxStudentCourseEntity traxStudentCourseEntity, String courseID){
    var optionalCourse = restUtils.getCoreg39CourseByID(courseID);
    if (optionalCourse.isPresent()) {
      var course = optionalCourse.get();
      traxStudentCourseEntity.getStudXcrseId().setCourseCode(course.getExternalCode().substring(0, 4));
      if(course.getExternalCode().length() > 5){
        traxStudentCourseEntity.getStudXcrseId().setCourseLevel(course.getExternalCode().substring(5));
      }
    }else{
      log.info("No course was found for ID {} :: this should not have happened", courseID);
      throw new PenReplicationAPIRuntimeException("No course was found for ID " + courseID + " :: this should not have happened");
    }
  }
  
  /**
   * Gets event type.
   *
   * @return the event type
   */
  @Override
  public String getEventType() {
    return UPDATE_STUDENT_COURSES.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentCourseUpdate studentCourseUpdate) {
    // Not required this child class use repository pattern of spring.
  }
}
