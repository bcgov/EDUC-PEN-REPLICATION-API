package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
  long countPenDemogTransactionByPenAndTransactionStatusInAndUpdateDateGreaterThan(String pen, List<String> transactionStatus, LocalDateTime dateTime);

  /**
   * Find all by transaction status order by transaction insert date time list.
   *
   * @param transactionStatus the transaction status
   * @return the list
   */
  List<PenDemogTransaction> findFirst10ByTransactionStatusAndTransactionTypeInOrderByTransactionInsertDateTime(String transactionStatus, List<String> transactionTypes);
}
