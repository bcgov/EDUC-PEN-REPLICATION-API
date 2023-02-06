package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.DistrictMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.DistrictMapperHelper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.DistrictMasterRepository;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.District;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_DISTRICT;

/**
 * The type School update service.
 */
@Service
@Slf4j
public class DistrictUpdateService extends BaseService<District> {

  private final DistrictMapperHelper districtMapperHelper;
  private final DistrictMasterRepository districtMasterRepository;

  private static final DistrictMapper districtMapper = DistrictMapper.mapper;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                    the emf
   * @param eventRepository        the event repository
   * @param districtMapperHelper
   * @param districtMasterRepository
   */
  public DistrictUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, final DistrictMapperHelper districtMapperHelper, DistrictMasterRepository districtMasterRepository) {
    super(eventRepository, emf);
    this.districtMapperHelper = districtMapperHelper;
    this.districtMasterRepository = districtMasterRepository;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final District district, final Event event) {
    log.info("Received and processing event: " + event.getEventId());

    val existingSchoolMasterRecord = this.districtMasterRepository.findById(district.getDistrictNumber());
    if (existingSchoolMasterRecord.isPresent()) {
      val existingDistrictMaster = existingSchoolMasterRecord.get();
      val newDistrictMaster = districtMapperHelper.toDistrictMaster(district);
      districtMapper.updateDistrictMaster(newDistrictMaster, existingDistrictMaster);
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), newDistrictMaster);
      districtMasterRepository.save(existingDistrictMaster);
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
    return UPDATE_DISTRICT.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final District district) {
    // Not required this child class use repository pattern of spring.
  }
}
