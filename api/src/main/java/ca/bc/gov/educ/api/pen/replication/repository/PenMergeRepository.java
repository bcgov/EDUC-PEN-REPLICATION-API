package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenMergePK;
import ca.bc.gov.educ.api.pen.replication.model.PenMergesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenMergeRepository extends JpaRepository<PenMergesEntity, PenMergePK> {
}
