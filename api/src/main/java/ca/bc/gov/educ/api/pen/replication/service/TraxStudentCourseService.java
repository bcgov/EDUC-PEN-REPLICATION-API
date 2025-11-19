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
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
  public void deletePriorAndSaveTraxStudentCourses(final List<TraxStudentCourseEntity> existingCourseList, final List<TraxStudentCourseEntity> studentCourseEntityList) {
    try {
      this.traxStudentCourseRepository.deleteAll(existingCourseList);
      this.traxStudentCourseRepository.saveAllAndFlush(studentCourseEntityList);
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
  }
  
}
