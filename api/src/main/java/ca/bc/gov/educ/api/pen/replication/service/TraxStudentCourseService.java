package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.repository.TraxStudentCourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TraxStudentCourseService {
  private final TraxStudentCourseRepository traxStudentCourseRepository;

  public TraxStudentCourseService(TraxStudentCourseRepository traxStudentCourseRepository) {
    this.traxStudentCourseRepository = traxStudentCourseRepository;
  }

  // it is saved in a new transaction to make sure commit happens in DB.
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
  public void deletePriorAndSaveTraxStudentCourses(final String pen, final List<TraxStudentCourseEntity> studentCourseEntityList) {
    try {
      this.traxStudentCourseRepository.deleteAllByStudXcrseId_StudNo(pen);
      this.traxStudentCourseRepository.saveAllAndFlush(studentCourseEntityList);
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
  }
  
}
