package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Pen audit entity.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PEN_AUDIT")
@IdClass(PenAuditPK.class)
public class PenAuditEntity implements Serializable {

  /**
   * The Activity date.
   */
  @Id
  @Column(name = "ACTIVITY_DATE")
  String activityDate;

  /**
   * The Audit code.
   */
  @Id
  @Column(name = "AUDIT_CODE")
  String auditCode;

  /**
   * The Local id.
   */
  @Column(name = "PEN_LOCAL_ID")
  String localID;

  /**
   * The Mincode.
   */
  @Column(name = "PEN_MINCODE")
  String mincode;

  /**
   * The Postal code.
   */
  @Column(name = "POSTAL")
  String postalCode;

  /**
   * The Dob.
   */
  @Column(name = "STUD_BIRTH")
  String dob;

  /**
   * The Demog code.
   */
  @Column(name = "STUD_DEMOG_CODE")
  String demogCode;

  /**
   * The Status code.
   */
  @Column(name = "STUD_STATUS")
  String statusCode;

  /**
   * The Pen.
   */
  @Id
  @Column(name = "STUD_NO")
  String pen;

  /**
   * The Legal first name.
   */
  @Column(name = "STUD_GIVEN")
  String legalFirstName;

  /**
   * The Legal middle names.
   */
  @Column(name = "STUD_MIDDLE")
  String legalMiddleNames;

  /**
   * The Legal last name.
   */
  @Column(name = "STUD_SURNAME")
  String legalLastName;

  /**
   * The Sex code.
   */
  @Column(name = "STUD_SEX")
  String sexCode;

  /**
   * The Usual first name.
   */
  @Column(name = "USUAL_GIVEN")
  String usualFirstName;

  /**
   * The Usual middle names.
   */
  @Column(name = "USUAL_MIDDLE")
  String usualMiddleNames;

  /**
   * The Usual last name.
   */
  @Column(name = "USUAL_SURNAME")
  String usualLastName;

  /**
   * The Grade code.
   */
  @Column(name = "STUD_GRADE")
  String gradeCode;

  /**
   * The Grade year.
   */
  @Column(name = "STUD_GRADE_YEAR")
  String gradeYear;

  /**
   * The Create user.
   */
  @Column(name = "USER_NAME")
  String createUser;


  /**
   * Gets id.
   *
   * @return the id
   */
  public PenAuditPK getId() {
    return new PenAuditPK(
      this.activityDate, this.auditCode, this.pen);
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(PenAuditPK id) {
    this.activityDate = id.getActivityDate();
    this.auditCode = id.getAuditCode();
    this.pen = id.getPen();
  }


}
