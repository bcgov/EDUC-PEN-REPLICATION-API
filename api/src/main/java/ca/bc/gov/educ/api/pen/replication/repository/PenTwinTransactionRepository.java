package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen twin transaction repository.
 */
@Repository
public interface PenTwinTransactionRepository extends JpaRepository<PenTwinTransaction, String> {

  /**
   * Count pen twin transaction by pen twin 1 and and pen twin 2 and and transaction status long.
   *
   * @param penTwin1          the pen twin 1
   * @param penTwin2          the pen twin 2
   * @param transactionStatus the transaction status
   * @return the long
   */
  long countPenTwinTransactionByPenTwin1AndPenTwin2AndTransactionStatus(String penTwin1, String penTwin2, String transactionStatus);

  /**
   * Find all by transaction status order by transaction insert date time list.
   *
   * @param transactionStatus the transaction status
   * @return the list
   */
  List<PenTwinTransaction> findFirst100ByTransactionStatusAndTransactionTypeInOrderByTransactionInsertDateTime(String transactionStatus, List<String> transactionTypes);

}
