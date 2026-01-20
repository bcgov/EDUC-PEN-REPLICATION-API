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

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public List<TraxStudentCourseEntity> findTraxStudentCoursesByPen(final String pen) {
    return this.traxStudentCourseRepository.findAllByStudXcrseId_StudNo(pen);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
  public void deleteTraxStudentCourses(final String pen) {
    try {
      if (!pen.isEmpty()) {
        log.debug("Removing existing course list for PEN: {}", pen);
        this.traxStudentCourseRepository.deleteAllByStudNoNative(pen);
        log.debug("Removed all existing trax student courses from the database for PEN: {}", pen);
      }
    } catch (Exception e) {
      log.warn("Exception while deleting courses", e);
      throw e;
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 180)
  public void saveTraxStudentCourses(final List<TraxStudentCourseEntity> studentCourseEntityList) {
    try {
      if (!studentCourseEntityList.isEmpty()) {
        log.debug("Saving new course list for PEN: {}, count: {}",
            studentCourseEntityList.get(0).getStudXcrseId().getStudNo(),
            studentCourseEntityList.size());
        traxStudentCourseRepository.saveAllAndFlush(studentCourseEntityList);
        log.debug("Saved new trax student courses to the database, count: {}", studentCourseEntityList.size());
      }
    } catch (Exception e) {
      log.warn("Exception while saving courses", e);
      throw e;
    }
  }
}
