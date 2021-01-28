package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PEN_AUDIT@PENLINK.WORLD")
@IdClass(PenAuditPK.class)
public class PenAuditEntity implements Serializable {

  @Id
  @Column(name = "ACTIVITY_DATE")
  String activityDate;

  @Id
  @Column(name = "AUDIT_CODE")
  String auditCode;

  @Column(name = "PEN_LOCAL_ID")
  String localID;

  @Column(name = "PEN_MINCODE")
  String mincode;

  @Column(name = "POSTAL")
  String postalCode;

  @Column(name = "STUD_BIRTH")
  String dob;

  @Column(name = "STUD_DEMOG_CODE")
  String demogCode;

  @Column(name = "STUD_STATUS")
  String statusCode;

  @Id
  @Column(name = "STUD_NO")
  String pen;

  @Column(name = "STUD_GIVEN")
  String legalFirstName;

  @Column(name = "STUD_MIDDLE")
  String legalMiddleNames;

  @Column(name = "STUD_SURNAME")
  String legalLastName;

  @Column(name = "STUD_SEX")
  String sexCode;

  @Column(name = "USUAL_GIVEN")
  String usualFirstName;

  @Column(name = "USUAL_MIDDLE")
  String usualMiddleNames;

  @Column(name = "USUAL_SURNAME")
  String usualLastName;

  @Column(name = "STUD_GRADE")
  String gradeCode;

  @Column(name = "STUD_GRADE_YEAR")
  String gradeYear;

  @Column(name = "USER_NAME")
  String createUser;


  public PenAuditPK getId() {
    return new PenAuditPK(
        this.activityDate, this.auditCode, this.pen);
  }

  public void setId(PenAuditPK id) {
    this.activityDate = id.getActivityDate();
    this.auditCode = id.getAuditCode();
    this.pen = id.getPen();
  }


}
