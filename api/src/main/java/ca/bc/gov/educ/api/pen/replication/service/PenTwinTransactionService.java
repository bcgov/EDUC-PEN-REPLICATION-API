package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Pen twin transaction service.
 */
@Service
@Slf4j
public class PenTwinTransactionService {
  private final SagaService sagaService;
  private final PenTwinTransactionRepository penTwinTransactionRepository;

  /**
   * Instantiates a new Pen twin transaction service.
   *
   * @param sagaService                  the saga service
   * @param penTwinTransactionRepository the pen twin transaction repository
   */
  public PenTwinTransactionService(final SagaService sagaService, final PenTwinTransactionRepository penTwinTransactionRepository) {
    this.sagaService = sagaService;
    this.penTwinTransactionRepository = penTwinTransactionRepository;
  }

  /**
   * Create saga and update pen twin transaction saga.
   *
   * @param sagaName           the saga name
   * @param userName           the user name
   * @param payload            the payload
   * @param penTwinTransaction the pen twin transaction
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Saga createSagaAndUpdatePenTwinTransaction(final String sagaName, final String userName, final String payload, final PenTwinTransaction penTwinTransaction) {
    this.penTwinTransactionRepository.findById(penTwinTransaction.getTransactionID()).ifPresent(twinTransaction -> {
      twinTransaction.setTransactionStatus(TransactionStatus.IN_PROGRESS.getCode());
      this.penTwinTransactionRepository.save(twinTransaction);
    });
    return this.sagaService.createSagaRecordInDB(sagaName, userName, payload, null);
  }
}
