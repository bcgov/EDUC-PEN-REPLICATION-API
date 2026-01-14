package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCourseUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_STUDENT_COURSES;

@Service
@Slf4j
public class StudentCourseUpdateService extends BaseService<StudentCourseUpdateSagaData> {

  private final SagaService sagaService;
  private final Orchestrator orchestrator;

  public StudentCourseUpdateService(final EntityManagerFactory emf,
                                    final EventRepository eventRepository,
                                    final SagaService sagaService,
                                    @Qualifier("studentCourseUpdateOrchestrator") final Orchestrator orchestrator) {
    super(eventRepository, emf);
    this.sagaService = sagaService;
    this.orchestrator = orchestrator;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final StudentCourseUpdateSagaData studentCourseUpdate, final Event event) {
    try {
      val conflictingSagas = sagaService.findInProgressStudentCourseUpdateSagasByStudentID(
        studentCourseUpdate.getStudentID()
      );

      val sagaPayload = JsonUtil.getJsonStringFromObject(studentCourseUpdate);
      val saga = sagaService.createSagaRecordInDB(
        SagaEnum.PEN_REPLICATION_STUDENT_COURSE_UPDATE_SAGA.getCode(),
        "REPLICATION_API",
        sagaPayload
      );

      if (!conflictingSagas.isEmpty()) {
        log.info("Queued student course update saga with ID {} for event ID {} (conflicting saga exists) :: studentID: {}",
                 saga.getSagaId(), event.getEventId(), studentCourseUpdate.getStudentID());
      } else {
        orchestrator.startSaga(saga);
        log.debug("Started student course update saga with ID {} for event ID {} :: payload is: {}",
                 saga.getSagaId(), event.getEventId(), studentCourseUpdate);
      }

      this.updateEvent(event);
    } catch (JsonProcessingException e) {
      log.error("Error creating saga payload for event ID: {}", event.getEventId(), e);
      throw new RuntimeException("Error creating saga payload", e);
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
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentCourseUpdateSagaData studentCourseUpdate) {
    // Not required this child class use repository pattern of spring.
  }
}
