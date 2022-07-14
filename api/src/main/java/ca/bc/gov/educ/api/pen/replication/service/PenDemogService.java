package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class PenDemogService {
  private final PenDemogRepository penDemogRepository;

  public PenDemogService(final PenDemogRepository penDemogRepository) {
    this.penDemogRepository = penDemogRepository;
  }

  // it is saved in a new transaction to make sure commit happens in DB.
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 50)
  public void savePenDemog(final PenDemographicsEntity entity) {
    try {
      this.penDemogRepository.saveAndFlush(entity);
    } catch (Exception e) {
      log.warn("Exception", e);
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public Optional<PenDemographicsEntity> findPenDemogByPen(final String pen) {
    return this.penDemogRepository.findById(pen);
  }
}
