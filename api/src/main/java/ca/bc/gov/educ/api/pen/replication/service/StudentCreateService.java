package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.util.ReplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_STUDENT;

/**
 * This class is responsible to get core student data
 */
@Service
@Slf4j
public class StudentCreateService extends BaseService {
  private final EntityManagerFactory emf;
  private final PenDemogRepository penDemogRepository;
  private final EventRepository eventRepository;

  @Autowired
  public StudentCreateService(EntityManagerFactory emf, PenDemogRepository penDemogRepository, EventRepository eventRepository) {
    this.emf = emf;
    this.penDemogRepository = penDemogRepository;
    this.eventRepository = eventRepository;
  }

  @Override
  public <T extends Object> void processEvent(T request, Event event) {
    StudentCreate studentCreate = (StudentCreate) request;
    PenDemographicsEntity penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentCreate);
    penDemographicsEntity.setCreateDate(formatDateTime(penDemographicsEntity.getCreateDate()));
    penDemographicsEntity.setUpdateDate(formatDateTime(penDemographicsEntity.getUpdateDate()));
    penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
    var existingPenDemogRecord = penDemogRepository.findById(StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10));
    EntityManager em = this.emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
      // below timeout is in milli seconds, so it is 10 seconds.
      if (existingPenDemogRecord.isEmpty()) {
        tx.begin();
        em.createNativeQuery(buildInsert(penDemographicsEntity)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
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


  @Override
  public String getEventType() {
    return CREATE_STUDENT.toString();
  }

  private String buildInsert(PenDemographicsEntity penDemographicsEntity) {
    String insert = "insert into pen_demog (create_date, create_user_name, stud_demog_code, stud_grade, stud_grade_year, pen_local_id, merge_to_code, merge_to_date, merge_to_user_name, pen_mincode, postal, stud_birth, stud_given, stud_middle, stud_sex, stud_status, stud_surname, stud_true_no, update_date, update_user_name, usual_given, usual_middle, usual_surname, stud_no) values (" +
        "TO_DATE('" + penDemographicsEntity.getCreateDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS')," +
        "'" + penDemographicsEntity.getCreateUser() + "'" + "," +
        "'" + (penDemographicsEntity.getDemogCode() == null ? "" : penDemographicsEntity.getDemogCode()) + "'" + "," +
        "'" + ReplicationUtils.getBlankWhenNull(penDemographicsEntity.getGrade()) + "'" + "," +
        "'" + (penDemographicsEntity.getGradeYear() == null ? "" : penDemographicsEntity.getGradeYear()) + "'" + "," +
        "'" + (penDemographicsEntity.getLocalID() == null ? "" : penDemographicsEntity.getLocalID()) + "'" + "," +
        "'" + "'" + "," +
        "'" + "'" + "," +
        "'" + "'" + "," +
        "'" + (penDemographicsEntity.getMincode() == null ? "" : penDemographicsEntity.getMincode()) + "'" + "," +
        "'" + (penDemographicsEntity.getPostalCode() == null ? " " : penDemographicsEntity.getPostalCode()) + "'" + "," +
        "'" + penDemographicsEntity.getStudBirth() + "'" + "," +
        "'" + (penDemographicsEntity.getStudGiven() == null ? "" : penDemographicsEntity.getStudGiven()) + "'" + "," +
        "'" + (penDemographicsEntity.getStudMiddle() == null ? "" : penDemographicsEntity.getStudMiddle()) + "'" + "," +
        "'" + penDemographicsEntity.getStudSex() + "'" + "," +
        "'" + (penDemographicsEntity.getStudStatus() == null ? "" : penDemographicsEntity.getStudStatus()) + "'" + "," +
        "'" + penDemographicsEntity.getStudSurname() + "'" + "," +
        "'" + "'" + "," +
        "TO_DATE('" + penDemographicsEntity.getUpdateDate() + "'" + ", 'YYYY-MM-DD HH24:MI:SS')," +
        "'" + penDemographicsEntity.getUpdateUser() + "'" + "," +
        "'" + (penDemographicsEntity.getUsualGiven() == null ? "" : penDemographicsEntity.getUsualGiven()) + "'" + "," +
        "'" + (penDemographicsEntity.getUsualMiddle() == null ? "" : penDemographicsEntity.getUsualMiddle()) + "'" + "," +
        "'" + (penDemographicsEntity.getUsualSurname() == null ? "" : penDemographicsEntity.getUsualSurname()) + "'" + "," +
        "'" + penDemographicsEntity.getStudNo() + "'" +
        ")";
    log.debug("Create Student: " + insert);
    return insert;
  }


}
