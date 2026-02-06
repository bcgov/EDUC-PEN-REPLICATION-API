package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TraxStudentCourseRepository extends JpaRepository<TraxStudentCourseEntity, String> {
    List<TraxStudentCourseEntity> findAllByStudNo(String studNo);
}
