package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.UPDATE_STUDENT;

/**
 * The type Student update service.
 */
@Service
@Slf4j
public class StudentUpdateService extends BaseService {
  /**
   * The Emf.
   */
  private final EntityManagerFactory emf;
  /**
   * The Pen demog repository.
   */
  private final PenDemogRepository penDemogRepository;
  /**
   * The Event repository.
   */
  private final EventRepository eventRepository;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;

  /**
   * Instantiates a new Student update service.
   *
   * @param emf                the emf
   * @param penDemogRepository the pen demog repository
   * @param eventRepository    the event repository
   * @param restUtils          the rest utils
   */
  public StudentUpdateService(EntityManagerFactory emf, PenDemogRepository penDemogRepository, EventRepository eventRepository, RestUtils restUtils) {
    this.emf = emf;
    this.penDemogRepository = penDemogRepository;
    this.eventRepository = eventRepository;
    this.restUtils = restUtils;
  }

  /**
   * Process event.
   *
   * @param <T>     the type parameter
   * @param request the request
   * @param event   the event
   */
  @Override
  public <T extends BaseRequest> void processEvent(T request, Event event) {
    EntityManager em = this.emf.createEntityManager();
    StudentUpdate studentUpdate = (StudentUpdate) request;
    PenDemographicsEntity penDemographicsEntity = getPenDemographicsEntity(studentUpdate);
    var existingPenDemogRecord = penDemogRepository.findById(StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10));

    EntityTransaction tx = em.getTransaction();

    try {
      // below timeout is in milli seconds, so it is 10 seconds.
      if (existingPenDemogRecord.isPresent()) {
        if (StringUtils.isNotBlank(studentUpdate.getTrueStudentID()) && StringUtils.isBlank(existingPenDemogRecord.get().getStudentTrueNo())) {
          penDemographicsEntity.setStudentTrueNo(getStudentPen(studentUpdate.getTrueStudentID()));
          penDemographicsEntity.setMergeToDate(studentUpdate.getUpdateDate());
        }else if(StringUtils.isBlank(studentUpdate.getTrueStudentID()) && StringUtils.isNotBlank(existingPenDemogRecord.get().getStudentTrueNo())){
          penDemographicsEntity.setStudentTrueNo(null);
          penDemographicsEntity.setMergeToDate(null);
        }
        tx.begin();
        em.createNativeQuery(buildUpdate(penDemographicsEntity)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
        tx.commit();
      }
      var existingEvent = eventRepository.findByEventId(event.getEventId());
      existingEvent.ifPresent(record -> {
        record.setEventStatus(PROCESSED.toString());
        record.setUpdateDate(LocalDateTime.now());
        eventRepository.save(record);
      });
    } catch (Exception e) {
      log.error("Error occurred saving entity " + e.getMessage());
      tx.rollback();
    } finally {
      if (em.isOpen()) {
        em.close();
      }
    }
  }

  /**
   * Gets student true pen number.
   *
   * @param trueStudentID the true student id
   * @return the student true number
   */
  private String getStudentPen(String trueStudentID) {
    return restUtils.getStudentPen(trueStudentID).orElseThrow();
  }

  /**
   * Gets pen demographics entity.
   *
   * @param studentUpdate the student update
   * @return the pen demographics entity
   */
  private PenDemographicsEntity getPenDemographicsEntity(StudentUpdate studentUpdate) {
    PenDemographicsEntity penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentUpdate);
    penDemographicsEntity.setCreateDate(formatDateTime(penDemographicsEntity.getCreateDate()));
    penDemographicsEntity.setUpdateDate(formatDateTime(penDemographicsEntity.getUpdateDate()));
    penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
    return penDemographicsEntity;
  }

  /**
   * Build update string.
   *
   * @param penDemographicsEntity the pen demographics entity
   * @return the string
   */
  private String buildUpdate(PenDemographicsEntity penDemographicsEntity) {
    return "UPDATE PEN_DEMOG SET "
        + "STUD_DEMOG_CODE=" + "'" + (penDemographicsEntity.getDemogCode() == null ? "" : penDemographicsEntity.getDemogCode()) + "'" + ","
        + "STUD_GRADE=" + "'" + (penDemographicsEntity.getGrade() == null ? "" : penDemographicsEntity.getGrade()) + "'" + ","
        + "STUD_GRADE_YEAR=" + "'" + (penDemographicsEntity.getGradeYear() == null ? "" : penDemographicsEntity.getGradeYear()) + "'" + ","
        + "PEN_LOCAL_ID=" + "'" + (penDemographicsEntity.getLocalID() == null ? "" : penDemographicsEntity.getLocalID()) + "'" + ","
        + "PEN_MINCODE=" + "'" + (penDemographicsEntity.getMincode() == null ? "" : penDemographicsEntity.getMincode()) + "'" + ","
        + "POSTAL=" + "'" + (penDemographicsEntity.getPostalCode() == null ? "" : penDemographicsEntity.getPostalCode()) + "'" + ","
        + "STUD_BIRTH=" + "'" + penDemographicsEntity.getStudBirth() + "'" + ","
        + "STUD_GIVEN=" + "'" + (penDemographicsEntity.getStudGiven() == null ? "" : penDemographicsEntity.getStudGiven()) + "'" + ","
        + "STUD_MIDDLE=" + "'" + (penDemographicsEntity.getStudMiddle() == null ? "" : penDemographicsEntity.getStudMiddle()) + "'" + ","
        + "STUD_SEX=" + "'" + penDemographicsEntity.getStudSex() + "'" + ","
        + "STUD_STATUS=" + "'" + (penDemographicsEntity.getStudStatus() == null ? "" : penDemographicsEntity.getStudStatus()) + "'" + ","
        + "STUD_SURNAME=" + "'" + penDemographicsEntity.getStudSurname() + "'" + ","
        + "MERGE_TO_DATE=" + penDemographicsEntity.getMergeToDate() == null ? "''" : "TO_DATE('" + penDemographicsEntity.getMergeToDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS')" + ","
        + "STUD_TRUE_NO=" + "'" + (penDemographicsEntity.getStudentTrueNo() == null ? "" : penDemographicsEntity.getStudentTrueNo()) + "'" + ","
        + "UPDATE_DATE=" + "TO_DATE('" + penDemographicsEntity.getUpdateDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS'),"
        + "UPDATE_USER_NAME=" + "'" + penDemographicsEntity.getUpdateUser() + "'" + ","
        + "USUAL_GIVEN=" + "'" + (penDemographicsEntity.getUsualGiven() == null ? "" : penDemographicsEntity.getUsualGiven()) + "'" + ","
        + "USUAL_MIDDLE=" + "'" + (penDemographicsEntity.getUsualMiddle() == null ? "" : penDemographicsEntity.getUsualMiddle()) + "'" + ","
        + "USUAL_SURNAME=" + "'" + (penDemographicsEntity.getUsualSurname() == null ? "" : penDemographicsEntity.getUsualSurname()) + "'"
        + " WHERE STUD_NO=" + "'" + penDemographicsEntity.getStudNo() + " '"; // a space is appended CAREFUL not to remove.
  }

  /**
   * Gets event type.
   *
   * @return the event type
   */
  @Override
  public String getEventType() {
    return UPDATE_STUDENT.toString();
  }
}
