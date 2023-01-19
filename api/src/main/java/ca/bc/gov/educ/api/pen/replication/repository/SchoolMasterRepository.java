package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolMasterRepository extends JpaRepository<SchoolMasterEntity, String> {
  Optional<SchoolMasterEntity> findByDistNoAndSchlNo(String distNo, String schlNo);
}
