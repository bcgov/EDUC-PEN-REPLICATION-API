package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_AUTHORITY;

@Service
@Slf4j
public class AuthorityCreateService extends BaseService<IndependentAuthority> {

  private final AuthorityMapperHelper authorityMapperHelper;
  private final AuthorityMasterRepository authorityMasterRepository;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  public AuthorityCreateService(final EntityManagerFactory emf, final EventRepository eventRepository, AuthorityMapperHelper authorityMapperHelper, AuthorityMasterRepository authorityMasterRepository) {
    super(eventRepository, emf);
    this.authorityMapperHelper = authorityMapperHelper;
    this.authorityMasterRepository = authorityMasterRepository;
  }

  @Override
  public void processEvent(final IndependentAuthority authority, final Event event) {
    log.info("Received and processing event: " + event.getEventId());

    if (StringUtils.isNotEmpty(authority.getOpenedDate()) && dateTimeMapper.map(authority.getOpenedDate()).isBefore(LocalDateTime.now())){
      val existingSchoolMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());
      if (!existingSchoolMasterRecord.isPresent()) {
        val newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, true);
        log.info("Processing choreography update event with ID {} :: payload is: {}", event.getEventId(), newAuthorityMaster);
        authorityMasterRepository.save(newAuthorityMaster);
      }
    }
    this.updateEvent(event);
  }

  @Override
  public String getEventType() {
    return CREATE_AUTHORITY.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final IndependentAuthority authority) {
    // Not required this child class use repository pattern of spring.
  }
}
