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
import org.springframework.jdbc.core.JdbcTemplate;
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
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  private final PenDemogService penDemogService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;
  private final JdbcTemplate jdbcTemplate;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                           the emf
   * @param penDemogRepository            the pen demog repository
   * @param eventRepository               the event repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param penDemogService               the pen demog service
   * @param restUtils                     the rest utils
   * @param jdbcTemplate
   */
  public StudentUpdateService(final EntityManagerFactory emf, final PenDemogRepository penDemogRepository, final EventRepository eventRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final PenDemogService penDemogService, final RestUtils restUtils, final JdbcTemplate jdbcTemplate) {
    super(eventRepository, emf);
    this.penDemogService = penDemogService;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.restUtils = restUtils;
    this.jdbcTemplate = jdbcTemplate;
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
    val existingPenDemogRecord = this.penDemogService.findPenDemogByPen(StringUtils.rightPad(studentUpdate.getPen(), 10));
    if (existingPenDemogRecord.isPresent()) {
      val existingPenDemog = existingPenDemogRecord.get();
      val penDemographicsEntity = PenReplicationHelper.getPenDemogFromStudentUpdate(studentUpdate, existingPenDemog, this.restUtils);
      val updateSql = PenReplicationHelper.buildPenDemogUpdatePS();
      this.jdbcTemplate.update(updateSql, penDemographicsEntity.getDemogCode(), penDemographicsEntity.getGrade(), penDemographicsEntity.getGradeYear(), penDemographicsEntity.getLocalID(), penDemographicsEntity.getMincode(), penDemographicsEntity.getPostalCode(), penDemographicsEntity.getStudBirth(), penDemographicsEntity.getStudGiven(), penDemographicsEntity.getStudMiddle(), penDemographicsEntity.getStudSex(), penDemographicsEntity.getStudStatus(), penDemographicsEntity.getStudSurname(), penDemographicsEntity.getMergeToDate(), penDemographicsEntity.getMergeToCode(), penDemographicsEntity.getStudentTrueNo(), penDemographicsEntity.getUpdateDate(), penDemographicsEntity.getUpdateUser(), penDemographicsEntity.getUsualGiven(), penDemographicsEntity.getUsualGiven(), penDemographicsEntity.getUsualMiddle(), penDemographicsEntity.getUsualSurname(), penDemographicsEntity.getStudNo());
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
