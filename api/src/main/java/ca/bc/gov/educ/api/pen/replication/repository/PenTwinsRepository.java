package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.PenTwinsEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinsEntityID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Pen twins repository.
 */
@Repository
public interface PenTwinsRepository extends JpaRepository<PenTwinsEntity, PenTwinsEntityID> {
}
