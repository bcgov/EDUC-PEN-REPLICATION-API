package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.repository.SchoolMasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class SchoolMasterService {
  private final SchoolMasterRepository schoolMasterRepository;

  public SchoolMasterService(SchoolMasterRepository schoolMasterRepository) {
    this.schoolMasterRepository = schoolMasterRepository;
  }

  // it is saved in a new transaction to make sure commit happens in DB.
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
  public SchoolMasterEntity saveSchool(final SchoolMasterEntity entity) {
    try {
      return this.schoolMasterRepository.saveAndFlush(entity);
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public Optional<SchoolMasterEntity> findSchoolByID(final Mincode mincode) {
    return this.schoolMasterRepository.findById(mincode);
  }
}
