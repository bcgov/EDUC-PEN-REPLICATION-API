package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Pen demog transaction repository.
 */
@Repository
public interface PenDemogTransactionRepository extends JpaRepository<PenDemogTransaction, String> {

  /**
   * Count pen demog transaction by pen and transaction status long.
   *
   * @param pen               the pen
   * @param transactionStatus the transaction status
   * @return the long
   */
  long countPenDemogTransactionByPenAndTransactionStatus(String pen, String transactionStatus);
}
