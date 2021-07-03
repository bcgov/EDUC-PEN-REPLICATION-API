package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Pen twin transaction repository.
 */
@Repository
public interface PenTwinTransactionRepository extends JpaRepository<PenTwinTransaction, String> {

  long countPenTwinTransactionByPenTwin1AndAndPenTwin2AndAndTransactionStatus(String penTwin1, String penTwin2, String transactionStatus);
}
