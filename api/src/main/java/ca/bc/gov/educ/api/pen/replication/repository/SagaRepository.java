package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.Saga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<Saga, UUID>, JpaSpecificationExecutor<Saga> {

  List<Saga> findAllByStatusInOrderByCreateDate(List<String> statuses);

  Optional<Saga> findByCreatedFromEventID(UUID createdFromEventID);

  long countSagasBySagaNameInAndStatusIn(List<String> sagaNames, List<String> statuses);

  @Transactional
  @Modifying
  @Query("delete from Saga where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
