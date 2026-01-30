package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.repository.TraxStudentCourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TraxStudentCourseService {
  private final TraxStudentCourseRepository traxStudentCourseRepository;

  public TraxStudentCourseService(TraxStudentCourseRepository traxStudentCourseRepository) {
    this.traxStudentCourseRepository = traxStudentCourseRepository;
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public List<TraxStudentCourseEntity> findTraxStudentCoursesByPen(final String pen) {
    return this.traxStudentCourseRepository.findAllByStudXcrseId_StudNo(pen);
  }

  // it is saved in a new transaction to make sure commit happens in DB.
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deletePriorAndSaveTraxStudentCourses(final List<TraxStudentCourseEntity> existingCourseList, final List<TraxStudentCourseEntity> studentCourseEntityList) {
    try {
      if(!existingCourseList.isEmpty()) {
        log.info("Removing existing course list for PEN: {}", existingCourseList.get(0).getStudXcrseId().getStudNo());
        this.traxStudentCourseRepository.deleteAll(existingCourseList);
      }

      if(!studentCourseEntityList.isEmpty()) {
        log.info("Removed all existing trax student courses from the database, PEN now has: {}", existingCourseList);
        this.traxStudentCourseRepository.saveAll(studentCourseEntityList);
      }
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
  }

}
