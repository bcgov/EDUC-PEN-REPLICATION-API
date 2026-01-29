package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
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
public class GradStudentUpdateService extends BaseService<StudentUpdate> {

  private final TraxStudentService traxStudentService;
  private final RestUtils restUtils;

  public GradStudentUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, final RestUtils restUtils) {
    super(eventRepository, emf);
    this.traxStudentService = traxStudentService;
    this.restUtils = restUtils;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final StudentUpdate studentUpdate, final Event event) {
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentUpdate.getPen(), 10));
    if (existingTraxStudentRecord.isPresent()) {
      val existingTraxStudent = existingTraxStudentRecord.get();
      existingTraxStudent.setStudSurname(studentUpdate.getLegalLastName());
      existingTraxStudent.setStudGiven(studentUpdate.getLegalFirstName());
      existingTraxStudent.setStudMiddle(studentUpdate.getLegalMiddleNames());
      existingTraxStudent.setStudSex(studentUpdate.getSexCode());
      existingTraxStudent.setStudSex(studentUpdate.getSexCode());
      existingTraxStudent.setStudBirth(StringUtils.replace(studentUpdate.getDob(), "-", ""));
      if (StringUtils.isNotBlank(studentUpdate.getTrueStudentID()) && StringUtils.isBlank(existingTraxStudent.getStudTrueNo())) {
        existingTraxStudent.setStudTrueNo(restUtils.getStudentPen(studentUpdate.getTrueStudentID()));
      }
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), existingTraxStudent);
      this.traxStudentService.saveTraxStudent(existingTraxStudent);
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
