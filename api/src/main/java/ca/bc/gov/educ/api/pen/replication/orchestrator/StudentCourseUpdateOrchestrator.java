package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.model.StudXcrseId;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.service.TraxStudentCourseService;
import ca.bc.gov.educ.api.pen.replication.service.TraxStudentService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCourse;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCourseUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ca.bc.gov.educ.api.pen.replication.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class StudentCourseUpdateOrchestrator extends BaseOrchestrator<StudentCourseUpdateSagaData> {

  private final TraxStudentCourseService traxStudentCourseService;
  private final TraxStudentService traxStudentService;
  private final RestUtils restUtils;

  public StudentCourseUpdateOrchestrator(final SagaService sagaService,
                                            final MessagePublisher messagePublisher, 
                                            final EntityManagerFactory entityManagerFactory,
                                            final TraxStudentCourseService traxStudentCourseService,
                                            final TraxStudentService traxStudentService,
                                            final RestUtils restUtils) {
    super(entityManagerFactory, sagaService, messagePublisher, StudentCourseUpdateSagaData.class, 
          SagaEnum.PEN_REPLICATION_STUDENT_COURSE_UPDATE_SAGA, 
          SagaTopicsEnum.PEN_REPLICATION_STUDENT_COURSE_UPDATE_SAGA_TOPIC);
    this.traxStudentCourseService = traxStudentCourseService;
    this.traxStudentService = traxStudentService;
    this.restUtils = restUtils;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(PREPARE_STUDENT_COURSE_UPDATE, this::prepareStudentCourseUpdate)
      .step(PREPARE_STUDENT_COURSE_UPDATE, STUDENT_COURSE_UPDATE_PREPARED, DELETE_STUDENT_COURSES, this::deleteStudentCourses)
      .step(PREPARE_STUDENT_COURSE_UPDATE, STUDENT_NOT_FOUND, MARK_SAGA_COMPLETE, this::markSagaComplete)
      .step(DELETE_STUDENT_COURSES, STUDENT_COURSES_DELETED, SAVE_STUDENT_COURSES, this::saveStudentCourses)
      .end(SAVE_STUDENT_COURSES, STUDENT_COURSES_SAVED);
  }

  private void prepareStudentCourseUpdate(final Event event, final Saga saga, final StudentCourseUpdateSagaData sagaData) {
    saga.setSagaState(PREPARE_STUDENT_COURSE_UPDATE.toString());
    saga.setStatus(IN_PROGRESS.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    
    try {
      var studentPEN = restUtils.getStudentPen(sagaData.getStudentID());
      val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
      
      if (existingTraxStudentRecord.isEmpty()) {
        log.error("Student course event not processed for saga {} :: student does not yet exist in TRAX STUDENT_MASTER", saga.getSagaId());
        throw new PenReplicationAPIRuntimeException("Student not found in TRAX with ID: " + studentPEN + "This should never happen.");
      }
      var existingCourses = this.traxStudentCourseService.findTraxStudentCoursesByPen(studentPEN);
      existingCourses = existingCourses.stream().filter(Objects::nonNull).toList();
      val newCourses = getStudentCourseEntityList(studentPEN, sagaData.getStudentCourses(), existingCourses);

      sagaData.setStudentPEN(studentPEN);
      sagaData.setNewCourses(newCourses);

      try {
        val updatedPayload = JsonUtil.getJsonStringFromObject(sagaData);
        saga.setPayload(updatedPayload);
        this.getSagaService().updateAttachedEntityDuringSagaProcess(saga);
      } catch (JsonProcessingException e) {
        log.error("Error updating saga payload for saga: {}", saga.getSagaId(), e);
        throw new RuntimeException("Error updating saga payload", e);
      }
      
      log.debug("Prepared student course update for PEN: {}, existing courses to delete: {}, new courses to save: {}",
               studentPEN, 
               existingCourses.size(),
               newCourses.size());
      
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(PREPARE_STUDENT_COURSE_UPDATE)
        .eventOutcome(STUDENT_COURSE_UPDATE_PREPARED)
        .eventPayload("PREPARED")
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent);
      log.debug("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), PREPARE_STUDENT_COURSE_UPDATE, saga.getSagaId());
    } catch (Exception e) {
      log.error("Error preparing student course update for saga: {}", saga.getSagaId(), e);
      throw e;
    }
  }

  private void deleteStudentCourses(final Event event, final Saga saga, final StudentCourseUpdateSagaData sagaData) {
    saga.setSagaState(DELETE_STUDENT_COURSES.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    
    try {
      this.traxStudentCourseService.deleteTraxStudentCourses(sagaData.getStudentPEN());

      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(DELETE_STUDENT_COURSES)
        .eventOutcome(STUDENT_COURSES_DELETED)
        .eventPayload("DELETED")
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent);
      log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), DELETE_STUDENT_COURSES, saga.getSagaId());
    } catch (Exception e) {
      log.error("Error deleting student courses for saga: {}", saga.getSagaId(), e);
      throw e;
    }
  }

  private void saveStudentCourses(final Event event, final Saga saga, final StudentCourseUpdateSagaData sagaData) {
    saga.setSagaState(SAVE_STUDENT_COURSES.toString());
    final SagaEvent eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    
    try {
      this.traxStudentCourseService.saveTraxStudentCourses(sagaData.getNewCourses());
      
      val nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(SAVE_STUDENT_COURSES)
        .eventOutcome(STUDENT_COURSES_SAVED)
        .eventPayload("SAVED")
        .build();
      this.postMessageToTopic(this.getTopicToSubscribe().getCode(), nextEvent);
      log.info("responded via NATS to {} for {} Event. :: {}", this.getTopicToSubscribe(), SAVE_STUDENT_COURSES, saga.getSagaId());
    } catch (Exception e) {
      log.error("Error saving student courses for saga: {}", saga.getSagaId(), e);
      throw e;
    }
  }

  private List<TraxStudentCourseEntity> getStudentCourseEntityList(String studentPEN, 
                                                                     List<StudentCourse> studentCourse, 
                                                                     List<TraxStudentCourseEntity> existingTraxStudentCourses) {
    var entityList = new ArrayList<TraxStudentCourseEntity>();
    studentCourse.forEach(student -> {
      TraxStudentCourseEntity traxStudentCourseEntity = new TraxStudentCourseEntity();
      traxStudentCourseEntity.setStudXcrseId(new StudXcrseId());
      setCourseCodeAndLevel(traxStudentCourseEntity, student.getCourseID());
      traxStudentCourseEntity.getStudXcrseId().setStudNo(StringUtils.trimToNull(studentPEN));
      traxStudentCourseEntity.getStudXcrseId().setCourseSession(StringUtils.trimToNull(student.getCourseSession()));
      traxStudentCourseEntity.setFinalLetterGrade(StringUtils.trimToNull(student.getFinalLetterGrade()));
      traxStudentCourseEntity.setFinalPercentage(student.getFinalPercent() != null ? student.getFinalPercent().toString() : null);
      traxStudentCourseEntity.setNumberOfCredits(student.getCredits() != null ? student.getCredits().toString() : null);
      setStudyTypeAndUsedForGradFields(traxStudentCourseEntity, existingTraxStudentCourses);
      entityList.add(traxStudentCourseEntity);
    });
    return entityList;
  }
  
  private void setStudyTypeAndUsedForGradFields(TraxStudentCourseEntity traxStudentCourseEntity, List<TraxStudentCourseEntity> existingTraxStudentCourses){
    for(TraxStudentCourseEntity course: existingTraxStudentCourses){
      if(course != null) {
        String courseCourseCode = StringUtils.trimToNull(course.getStudXcrseId().getCourseCode());
        String courseCourseLevel = StringUtils.trimToNull(course.getStudXcrseId().getCourseLevel());
        String courseCourseSession = StringUtils.trimToNull(course.getStudXcrseId().getCourseSession());

        String traxCourseCode = StringUtils.trimToNull(traxStudentCourseEntity.getStudXcrseId().getCourseCode());
        String traxCourseLevel = StringUtils.trimToNull(traxStudentCourseEntity.getStudXcrseId().getCourseLevel());
        String traxCourseSession = StringUtils.trimToNull(traxStudentCourseEntity.getStudXcrseId().getCourseSession());

        if (Objects.equals(courseCourseCode, traxCourseCode) &&
                Objects.equals(courseCourseLevel, traxCourseLevel) &&
                Objects.equals(courseCourseSession, traxCourseSession)) {
          traxStudentCourseEntity.setStudyType(course.getStudyType());
          traxStudentCourseEntity.setUsedForGrad(course.getUsedForGrad());
          break;
        }
      }
    }
  }
  
  private void setCourseCodeAndLevel(TraxStudentCourseEntity traxStudentCourseEntity, String courseID){
    var optionalCourse = restUtils.getCoreg39CourseByID(courseID);
    if (optionalCourse.isPresent()) {
      var course = optionalCourse.get();
      if(course.getExternalCode().length() > 5) {
        traxStudentCourseEntity.getStudXcrseId().setCourseCode(StringUtils.trimToNull(course.getExternalCode().substring(0, 4)));
        traxStudentCourseEntity.getStudXcrseId().setCourseLevel(StringUtils.trimToNull(course.getExternalCode().substring(5)));
      }else{
        traxStudentCourseEntity.getStudXcrseId().setCourseCode(StringUtils.trimToNull(course.getExternalCode()));
        traxStudentCourseEntity.getStudXcrseId().setCourseLevel("   ");//trax needs the whitespace
      }
    }else{
      log.info("No course was found for ID {} :: this should not have happened", courseID);
      throw new PenReplicationAPIRuntimeException("No course was found for ID " + courseID + " :: this should not have happened");
    }
  }
}

