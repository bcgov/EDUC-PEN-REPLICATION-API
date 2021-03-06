package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.helpers.PenReplicationHelper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
  private final PenDemogRepository penDemogRepository;
  private final PenDemogTransactionRepository penDemogTransactionRepository;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                           the emf
   * @param penDemogRepository            the pen demog repository
   * @param eventRepository               the event repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param restUtils                     the rest utils
   */
  public StudentUpdateService(final EntityManagerFactory emf, final PenDemogRepository penDemogRepository, final EventRepository eventRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final RestUtils restUtils) {
    super(eventRepository, emf);
    this.penDemogRepository = penDemogRepository;
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
      log.info("This event is part of orchestrator flow, marking it processed.");
      return;
    }
    val existingPenDemogRecord = this.penDemogRepository.findById(StringUtils.rightPad(studentUpdate.getPen(), 10));
    if (existingPenDemogRecord.isPresent()) {
      super.persistData(event, studentUpdate);
    } else {
      this.updateEvent(event);
    }
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
    val existingPenDemogRecord = this.penDemogRepository.findById(StringUtils.rightPad(studentUpdate.getPen(), 10));
    existingPenDemogRecord.ifPresent(penDemographicsEntity -> em.createNativeQuery(PenReplicationHelper.buildPenDemogUpdate(studentUpdate, penDemographicsEntity, this.restUtils)).setHint("javax.persistence.query.timeout", 10000).executeUpdate());
  }
}
