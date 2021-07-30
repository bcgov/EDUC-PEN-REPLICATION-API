package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_STUDENT;

/**
 * This class is responsible to get core student data
 */
@Service
@Slf4j
public class StudentCreateService extends BaseService<StudentCreate> {
  private final PenDemogService penDemogService;
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  /**
   * Instantiates a new Student create service.
   *
   * @param emf                           the emf
   * @param penDemogRepository            the pen demog repository
   * @param eventRepository               the event repository
   * @param penDemogService               the pen demog service
   * @param penDemogTransactionRepository the pen demog transaction repository
   */
  @Autowired
  public StudentCreateService(final EntityManagerFactory emf, final PenDemogRepository penDemogRepository, final EventRepository eventRepository, PenDemogService penDemogService, final PenDemogTransactionRepository penDemogTransactionRepository) {
    super(eventRepository, emf);
    this.penDemogService = penDemogService;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
  }

  @Override
  public void processEvent(final StudentCreate request, final Event event) {
    if (this.isEventPartOfOrchestratorSaga(this.penDemogTransactionRepository, StringUtils.trim(request.getPen()))) {
      this.updateEvent(event);
      log.info("This event is part of orchestrator flow, marking it processed.");
      return;
    }
    final var existingPenDemogRecord = this.penDemogService.findPenDemogByPen(StringUtils.rightPad(request.getPen(), 10));
    if (existingPenDemogRecord.isEmpty()) {
      val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(request);
      penDemographicsEntity.setCreateDate(formatDateTime(penDemographicsEntity.getCreateDate()));
      penDemographicsEntity.setUpdateDate(formatDateTime(penDemographicsEntity.getUpdateDate()));
      penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
      this.penDemogService.savePenDemog(penDemographicsEntity);
    }
    this.updateEvent(event);

  }


  @Override
  public String getEventType() {
    return CREATE_STUDENT.toString();
  }


  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentCreate studentCreate) {
    // Not required this child class use repository pattern of spring.
  }
}
