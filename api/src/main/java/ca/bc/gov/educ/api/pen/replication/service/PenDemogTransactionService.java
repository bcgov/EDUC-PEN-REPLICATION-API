package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Pen demog transaction service.
 */
@Service
@Slf4j
public class PenDemogTransactionService {

  private final SagaService sagaService;
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  /**
   * Instantiates a new Pen demog transaction service.
   *
   * @param sagaService                   the saga service
   * @param penDemogTransactionRepository the pen demog transaction repository
   */
  public PenDemogTransactionService(final SagaService sagaService, final PenDemogTransactionRepository penDemogTransactionRepository) {
    this.sagaService = sagaService;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
  }

  /**
   * Create saga and update pen demog transaction saga.
   *
   * @param sagaName            the saga name
   * @param userName            the user name
   * @param payload             the payload
   * @param penDemogTransaction the pen demog transaction
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Saga createSagaAndUpdatePenDemogTransaction(final String sagaName, final String userName, final String payload, final PenDemogTransaction penDemogTransaction) {
    this.penDemogTransactionRepository.findById(penDemogTransaction.getTransactionID()).ifPresent(demogTransaction -> {
      demogTransaction.setTransactionStatus(TransactionStatus.IN_PROGRESS.getCode());
      this.penDemogTransactionRepository.save(demogTransaction);
    });
    return this.sagaService.createSagaRecordInDB(sagaName, userName, payload);
  }
}
