package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.Saga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<Saga, UUID>, JpaSpecificationExecutor<Saga> {
  /**
   * Find all by status in list.
   *
   * @param statuses the statuses
   * @return the list
   */
  List<Saga> findAllByStatusInOrderByCreateDate(List<String> statuses);


  /**
   * Find all by create date before list.
   *
   * @param createDate the create date
   * @return the list
   */
  List<Saga> findAllByCreateDateBefore(LocalDateTime createDate);

  long countSagasBySagaNameInAndStatusIn(List<String> sagaNames, List<String> statuses);
}
