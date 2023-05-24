package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.UPDATE_AUTHORITY;

@Service
@Slf4j
public class AuthorityUpdateService extends BaseService<IndependentAuthority> {

  private final AuthorityMapperHelper authorityMapperHelper;
  private final AuthorityMasterRepository authorityMasterRepository;

  private static final AuthorityMapper authorityMapper = AuthorityMapper.mapper;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                       the emf
   * @param eventRepository           the event repository
   * @param authorityMapperHelper
   * @param authorityMasterRepository
   */
  public AuthorityUpdateService(final EntityManagerFactory emf, final EventRepository eventRepository, AuthorityMapperHelper authorityMapperHelper, AuthorityMasterRepository authorityMasterRepository) {
    super(eventRepository, emf);
    this.authorityMapperHelper = authorityMapperHelper;
    this.authorityMasterRepository = authorityMasterRepository;
  }

  /**
   * Process event.
   *
   * @param event the event
   */
  @Override
  public void processEvent(final IndependentAuthority authority, final Event event) {
    log.info("Received and processing event: " + event.getEventId());

    val existingSchoolMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());
    if (existingSchoolMasterRecord.isPresent()) {
      val existingAuthorityMaster = existingSchoolMasterRecord.get();
      val newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, false);
      authorityMapper.updateAuthorityMaster(newAuthorityMaster, existingAuthorityMaster);
      log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), newAuthorityMaster);
      authorityMasterRepository.save(existingAuthorityMaster);
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
    return UPDATE_AUTHORITY.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final IndependentAuthority authority) {
    // Not required this child class use repository pattern of spring.
  }
}
