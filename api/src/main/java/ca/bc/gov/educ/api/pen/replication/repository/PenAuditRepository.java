package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenAuditEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenAuditPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen audit repository.
 */
@Repository
public interface PenAuditRepository extends JpaRepository<PenAuditEntity, PenAuditPK> {
  /**
   * Find all by pen list.
   *
   * @param pen the pen
   * @return the list
   */
  List<PenAuditEntity> findAllByPen(String pen);
}
