package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Pen demog repository.
 */
@Repository
public interface PenDemogRepository extends JpaRepository<PenDemographicsEntity, String> {
}
