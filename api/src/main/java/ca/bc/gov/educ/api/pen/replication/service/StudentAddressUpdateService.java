package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.StudentAddress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_STUDENT_ADDRESS;

/**
 * The type Student Address update or create service.
 */
@Service
@Slf4j
public class StudentAddressUpdateService extends BaseService<StudentAddress> {

  private final TraxStudentService traxStudentService;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  public StudentAddressUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, final RestUtils restUtils) {
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
  public void processEvent(final StudentAddress studentAddress, final Event event) {
    var studentPEN = restUtils.getStudentPen(studentAddress.getStudentID());
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentPEN, 10));
    if (existingTraxStudentRecord.isPresent()) {
      val existingTraxStudent = existingTraxStudentRecord.get();
      existingTraxStudent.setAddress1(studentAddress.getAddressLine1());
      existingTraxStudent.setAddress2(studentAddress.getAddressLine2());
      existingTraxStudent.setCity(studentAddress.getCity());
      existingTraxStudent.setProvCode(studentAddress.getProvinceStateCode());
      existingTraxStudent.setCntryCode(studentAddress.getCountryCode());
      existingTraxStudent.setPostal(studentAddress.getPostalZip());
      
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
    return UPDATE_STUDENT_ADDRESS.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentAddress studentAddress) {
    // Not required this child class use repository pattern of spring.
  }
}
