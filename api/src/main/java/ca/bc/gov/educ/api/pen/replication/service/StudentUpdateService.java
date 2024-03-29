package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_STUDENT;

/**
 * The type Student update service.
 */
@Service
@Slf4j
public class StudentUpdateService extends BaseService<StudentUpdate> {
  /**
   * The Pen demog repository.
   */
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  private final PenDemogService penDemogService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                           the emf
   * @param eventRepository               the event repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param penDemogService               the pen demog service
   * @param restUtils                     the rest utils
   */
  public StudentUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final PenDemogService penDemogService, final RestUtils restUtils) {
    super(eventRepository, emf);
    this.penDemogService = penDemogService;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.restUtils = restUtils;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final StudentUpdate studentUpdate, final Event event) {
    if (this.isEventPartOfOrchestratorSaga(this.penDemogTransactionRepository, StringUtils.trim(studentUpdate.getPen()))) {
      this.updateEvent(event);
      log.info("Update event with ID " + event.getEventId() + " is part of a SAGA and has been updated to processed with no action taken.");
      return;
    }
    val existingPenDemogRecord = this.penDemogService.findPenDemogByPen(StringUtils.rightPad(studentUpdate.getPen(), 10));
    if (existingPenDemogRecord.isPresent()) {
      val existingPenDemog = existingPenDemogRecord.get();
      val penDemographicsEntity = PenReplicationHelper.getPenDemogFromStudentUpdate(studentUpdate, existingPenDemog, this.restUtils);
      PenDemogStudentMapper.mapper.updatePenDemog(penDemographicsEntity, existingPenDemog);
      if (StringUtils.isNotBlank(studentUpdate.getGradeYear()) && StringUtils.isNumeric(studentUpdate.getGradeYear())) {
        existingPenDemog.setGradeYear(studentUpdate.getGradeYear());
      }
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), penDemographicsEntity);
      this.penDemogService.savePenDemog(existingPenDemog);
    }
    this.updateEvent(event);
  }


  /**
   * Gets event type.
   *
   * @return the event type
   */
  @Override
  public String getEventType() {
    return UPDATE_STUDENT.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentUpdate studentUpdate) {
    // Not required this child class use repository pattern of spring.
  }
}
