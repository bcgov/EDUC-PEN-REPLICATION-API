package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GraduationStudentRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_GRAD_STUDENT_CITIZENSHIP;

@Service
@Slf4j
public class GradStudentCitizenshipUpdateService extends BaseService<GraduationStudentRecord> {

  private final TraxStudentService traxStudentService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  public GradStudentCitizenshipUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, final RestUtils restUtils) {
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
  public void processEvent(final GraduationStudentRecord studentRecord, final Event event) {
    var studentPEN = restUtils.getStudentPen(studentRecord.getStudentID().toString());
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
    if (existingTraxStudentRecord.isPresent()) {
      val existingTraxStudent = existingTraxStudentRecord.get();
      existingTraxStudent.setStudCitiz(studentRecord.getStudentCitizenship());
      
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
    return UPDATE_GRAD_STUDENT_CITIZENSHIP.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final GraduationStudentRecord gradStudentRecord) {
    // Not required this child class use repository pattern of spring.
  }
}
