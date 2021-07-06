package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PenDemogTransactionService {

  private final SagaService sagaService;
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  public PenDemogTransactionService(final SagaService sagaService, final PenDemogTransactionRepository penDemogTransactionRepository) {
    this.sagaService = sagaService;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Saga createSagaAndUpdatePenDemogTransaction(final String sagaName, final String userName, final String payload, final PenDemogTransaction penDemogTransaction) {
    this.penDemogTransactionRepository.findById(penDemogTransaction.getTransactionID()).ifPresent(demogTransaction -> {
      demogTransaction.setTransactionStatus(TransactionStatus.IN_PROGRESS.getCode());
      this.penDemogTransactionRepository.save(demogTransaction);
    });
    return this.sagaService.createSagaRecordInDB(sagaName, userName, payload);
  }
}
