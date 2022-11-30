package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The interface Pen demog transaction repository.
 */
@Repository
public interface PenDemogTransactionRepository extends JpaRepository<PenDemogTransaction, String> {

  @Transactional
  @Query("select count(tran) from PenDemogTransaction tran where trim(tran.pen) = :pen " +
    "AND tran.transactionStatus in (:transactionStatus) " +
    "AND (tran.transactionProcessedDateTime >= :dateTime OR tran.transactionProcessedDateTime is null)")
  long countPenDemogTransactionByPen(@Param("pen") String pen, @Param("transactionStatus") List<String> transactionStatus, @Param("dateTime")LocalDateTime dateTime);

  /**
   * Find all by transaction status order by transaction insert date time list.
   *
   * @param transactionStatus the transaction status
   * @return the list
   */
  List<PenDemogTransaction> findFirst10ByTransactionStatusAndTransactionTypeInOrderByTransactionInsertDateTime(String transactionStatus, List<String> transactionTypes);
}
