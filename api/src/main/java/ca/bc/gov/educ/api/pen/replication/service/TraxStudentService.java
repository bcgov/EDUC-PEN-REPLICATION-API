package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.repository.TraxStudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class TraxStudentService {
  private final TraxStudentRepository traxStudentRepository;

  public TraxStudentService(TraxStudentRepository traxStudentRepository) {
      this.traxStudentRepository = traxStudentRepository;
  }

  // it is saved in a new transaction to make sure commit happens in DB.
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
  public void saveTraxStudent(final TraxStudentEntity entity) {
    try {
      this.traxStudentRepository.saveAndFlush(entity);
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public Optional<TraxStudentEntity> findTraxStudentByPen(final String pen) {
    return this.traxStudentRepository.findById(pen);
  }
}
