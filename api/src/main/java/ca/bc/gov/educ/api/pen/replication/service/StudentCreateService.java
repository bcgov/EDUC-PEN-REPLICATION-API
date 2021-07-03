package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.util.ReplicationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_STUDENT;

/**
 * This class is responsible to get core student data
 */
@Service
@Slf4j
public class StudentCreateService extends BaseService<StudentCreate> {
  private final PenDemogRepository penDemogRepository;
  private final PenDemogTransactionRepository penDemogTransactionRepository;

  /**
   * Instantiates a new Student create service.
   *
   * @param emf                           the emf
   * @param penDemogRepository            the pen demog repository
   * @param eventRepository               the event repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   */
  @Autowired
  public StudentCreateService(final EntityManagerFactory emf, final PenDemogRepository penDemogRepository, final EventRepository eventRepository, final PenDemogTransactionRepository penDemogTransactionRepository) {
    super(eventRepository, emf);
    this.penDemogRepository = penDemogRepository;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
  }

  @Override
  public void processEvent(final StudentCreate request, final Event event) {
    if (this.isEventPartOfOrchestratorSaga(this.penDemogTransactionRepository, request.getPen())) {
      this.updateEvent(event);
      log.info("This event is part of orchestrator flow, marking it processed.");
      return;
    }
    final var existingPenDemogRecord = this.penDemogRepository.findById(StringUtils.rightPad(request.getPen(), 10));
    if (existingPenDemogRecord.isEmpty()) {
      super.persistData(event, request);
    } else {
      this.updateEvent(event);
    }

  }


  @Override
  public String getEventType() {
    return CREATE_STUDENT.toString();
  }

  private String buildInsert(final PenDemographicsEntity penDemographicsEntity) {
    final String insert = "insert into pen_demog (create_date, create_user_name, stud_demog_code, stud_grade, stud_grade_year, pen_local_id, merge_to_code, merge_to_date, merge_to_user_name, pen_mincode, postal, stud_birth, stud_given, stud_middle, stud_sex, stud_status, stud_surname, stud_true_no, update_date, update_user_name, usual_given, usual_middle, usual_surname, stud_no) values (" +
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


  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final StudentCreate studentCreate) {
    val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentCreate);
    penDemographicsEntity.setCreateDate(this.formatDateTime(penDemographicsEntity.getCreateDate()));
    penDemographicsEntity.setUpdateDate(this.formatDateTime(penDemographicsEntity.getUpdateDate()));
    penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));
    em.createNativeQuery(this.buildInsert(penDemographicsEntity)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
  }
}
