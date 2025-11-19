package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GraduationStudentRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.ADOPT_GRAD_STUDENT;

@Service
@Slf4j
public class GradStudentAdoptService extends BaseService<GraduationStudentRecord> {
  private final TraxStudentService traxStudentService;
  private final RestUtils restUtils;
  
  @Autowired
  public GradStudentAdoptService(final EntityManagerFactory emf, final EventRepository eventRepository, TraxStudentService traxStudentService, RestUtils restUtils) {
    super(eventRepository, emf);
    this.traxStudentService = traxStudentService;
    this.restUtils = restUtils;
  }

  @Override
  public void processEvent(final GraduationStudentRecord student, final Event event) {
    var studentFromStudentAPIList = this.restUtils.getStudentsByID(List.of(student.getStudentID().toString()));
    val existingTraxStudentRecord = this.traxStudentService.findTraxStudentByPen(StringUtils.rightPad(studentFromStudentAPIList.get(student.getStudentID().toString()).getPen(), 10));
    var schoolOfRecord = this.restUtils.getSchoolBySchoolID(student.getSchoolOfRecordId().toString());
    var gradProgramCode = StringUtils.isNotBlank(student.getProgram()) ? student.getProgram().substring(0, 4) : null;
    var studentAddress = restUtils.getStudentScholarshipAddressByStudentID(UUID.randomUUID(), student.getStudentID().toString());
    
    String mincodeGrad;
    String gradDate;
    if(gradProgramCode != null && gradProgramCode.equalsIgnoreCase("SCCP")){
      mincodeGrad = null;
      gradDate = null;
    }else{
      mincodeGrad = restUtils.getSchoolBySchoolID(student.getSchoolAtGradId().toString()).get().getMincode() ;
      gradDate = StringUtils.isNotBlank(student.getProgramCompletionDate()) ?  student.getProgramCompletionDate().substring(0,7).replace("-","") : null ;
    }
    
    if (!existingTraxStudentRecord.isPresent() && !studentFromStudentAPIList.isEmpty()) {
      var stud = studentFromStudentAPIList.get(student.getStudentID().toString());
      TraxStudentEntity traxStudentEntity = new TraxStudentEntity();
      traxStudentEntity.setStudNo(student.getPen());
      traxStudentEntity.setArchiveFlag(getTraxArchiveFlag(student.getStudentStatus()));
      traxStudentEntity.setStudSurname(stud.getLegalLastName());
      traxStudentEntity.setStudGiven(stud.getLegalFirstName());
      traxStudentEntity.setStudMiddle(stud.getLegalMiddleNames());
      traxStudentEntity.setAddress1(studentAddress.isPresent() ? studentAddress.get().getAddressLine1() : null);
      traxStudentEntity.setAddress2(studentAddress.isPresent() ? studentAddress.get().getAddressLine2() : null);
      traxStudentEntity.setCity(studentAddress.isPresent() ? studentAddress.get().getCity() : null);
      traxStudentEntity.setProvCode(studentAddress.isPresent() ? studentAddress.get().getProvinceStateCode() : null);
      traxStudentEntity.setCntryCode(studentAddress.isPresent() ? studentAddress.get().getCountryCode() : null);
      traxStudentEntity.setPostal(studentAddress.isPresent() ? studentAddress.get().getPostalZip() : null);
      traxStudentEntity.setStudBirth(stud.getDob().replace("-", ""));
      traxStudentEntity.setStudSex(stud.getSexCode());
      traxStudentEntity.setStudCitiz(student.getStudentCitizenship());
      traxStudentEntity.setStudGrade(student.getStudentGrade());
      traxStudentEntity.setMincode(schoolOfRecord.get().getMincode());
      traxStudentEntity.setStudStatus(getTraxStudentStatus(student.getStudentStatus()));
      traxStudentEntity.setGradDate(StringUtils.isNotBlank(gradDate) ? Long.valueOf(gradDate) : null);
      traxStudentEntity.setMincodeGrad(mincodeGrad);
      traxStudentEntity.setGradReqtYear(gradProgramCode);
      
      log.info("Processing choreography adopt GRAD student event with ID {} :: payload is: {}", event.getEventId(), traxStudentEntity);
      this.traxStudentService.saveTraxStudent(traxStudentEntity);
    }
    this.updateEvent(event);

  }

  private String getTraxArchiveFlag(String gradStudentStatus){
    if(gradStudentStatus.equalsIgnoreCase("CUR") || gradStudentStatus.equalsIgnoreCase("TER")){
      return "A";
    }
    return "I";
  }
  
  private String getTraxStudentStatus(String gradStudentStatus){
    if(gradStudentStatus.equalsIgnoreCase("CUR") || gradStudentStatus.equalsIgnoreCase("ARC")){
      return "A";
    }else if(gradStudentStatus.equalsIgnoreCase("DEC")){
      return "D";
    }else if(gradStudentStatus.equalsIgnoreCase("MER")){
      return "M";
    }else if(gradStudentStatus.equalsIgnoreCase("TER")){
      return "T";
    }
    return "A";
  }
  
  @Override
  public String getEventType() {
    return ADOPT_GRAD_STUDENT.toString();
  }

  @Override
  protected void buildAndExecutePreparedStatements(final EntityManager em, final GraduationStudentRecord graduationStudentRecord) {
    // Not required this child class use repository pattern of spring.
  }
}
