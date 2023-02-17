package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapperHelper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SchoolMasterRepository;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_SCHOOL;

/**
 * The type School update service.
 */
@Service
@Slf4j
public class SchoolCreateService extends BaseService<School> {

  private final SchoolMapperHelper schoolMapperHelper;
  private final SchoolMasterRepository schoolMasterRepository;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();
  /**
   * Instantiates a new Student update service.
   *
   * @param emf                    the emf
   * @param eventRepository        the event repository
   * @param schoolMapperHelper
   * @param schoolMasterRepository
   */
  public SchoolCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, final SchoolMapperHelper schoolMapperHelper, SchoolMasterRepository schoolMasterRepository) {
    super(eventRepository, emf);
    this.schoolMapperHelper = schoolMapperHelper;
    this.schoolMasterRepository = schoolMasterRepository;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final School school, final Event event) {
    log.info("Received and processing event: " + event.getEventId());

    if (StringUtils.isNotEmpty(school.getOpenedDate()) && dateTimeMapper.map(school.getOpenedDate()).isBefore(LocalDateTime.now())){
      var mincode = new Mincode();
      mincode.setSchlNo(school.getSchoolNumber());
      mincode.setDistNo(school.getMincode().substring(0,3));
      val existingSchoolMasterRecord = this.schoolMasterRepository.findById(mincode);
      if (!existingSchoolMasterRecord.isPresent()) {
        val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, true);
        log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), newSchoolMaster);
        schoolMasterRepository.save(newSchoolMaster);
      }
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
    return CREATE_SCHOOL.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final School school) {
    // Not required this child class use repository pattern of spring.
  }
}
