package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraxStudentCourseRepository extends JpaRepository<TraxStudentCourseEntity, String> {
    void deleteAllByStudNo(String studNo);
}
