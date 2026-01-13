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
    void deleteAllByStudXcrseId_StudNo(String studNo);
    List<TraxStudentCourseEntity> findAllByStudXcrseId_StudNo(String studNo);

    @Modifying
    @Query(value = "DELETE FROM STUD_XCRSE WHERE STUD_NO = ?", nativeQuery = true)
    @Transactional
    void deleteAllByStudNoNative(String studNo);
}
