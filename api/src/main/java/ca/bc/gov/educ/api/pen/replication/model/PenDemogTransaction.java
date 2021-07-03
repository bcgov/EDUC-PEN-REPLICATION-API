package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The type Pen demog transaction.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "PEN_DEMOG_TX")
public class PenDemogTransaction implements Serializable {

  private static final long serialVersionUID = 7763011484287937329L;

  /**
   * The Transaction id.
   */
  @Id
  @Column(name = "TX_ID", unique = true, updatable = false, length = 10)
  String transactionID;

  /**
   * The Transaction type.
   */
  @Column(name = "TX_TYPE", length = 4)
  String transactionType;

  /**
   * The Transaction status.
   */
  @Column(name = "TX_STATUS", length = 4)
  String transactionStatus;

  /**
   * The Transaction insert date time.
   */
  @Column(name = "TX_INSERT_DATE_TIME")
  LocalDateTime transactionInsertDateTime;

  /**
   * The Transaction processed date time.
   */
  @Column(name = "TX_PROCESSED_DATE_TIME")
  LocalDateTime transactionProcessedDateTime;

  /**
   * The Pen.
   */
  @Column(name = "STUD_NO", length = 10)
  String pen;

  /**
   * The Surname.
   */
  @Column(name = "STUD_SURNAME", length = 25)
  String surname;
  /**
   * The Given name.
   */
  @Column(name = "STUD_GIVEN", length = 25)
  String givenName;
  /**
   * The Middle name.
   */
  @Column(name = "STUD_MIDDLE", length = 25)
  String middleName;

  /**
   * The Usual surname.
   */
  @Column(name = "USUAL_SURNAME", length = 25)
  String usualSurname;
  /**
   * The Usual given name.
   */
  @Column(name = "USUAL_GIVEN", length = 25)
  String usualGivenName;
  /**
   * The Usual middle name.
   */
  @Column(name = "USUAL_MIDDLE", length = 25)
  String usualMiddleName;

  /**
   * The Birth date.
   */
  @Column(name = "STUD_BIRTH", length = 8)
  String birthDate; // YYYYMMDD

  /**
   * The Sex.
   */
  @Column(name = "STUD_SEX", length = 1)
  String sex;

  /**
   * The Demog code.
   */
  @Column(name = "STUD_DEMOG_CODE", length = 1)
  String demogCode;

  /**
   * The Status.
   */
  @Column(name = "STUD_STATUS", length = 1)
  String status;

  /**
   * The Pen local id.
   */
  @Column(name = "PEN_LOCAL_ID", length = 12)
  String penLocalID;

  /**
   * The Pen min code.
   */
  @Column(name = "PEN_MINCODE", length = 8)
  String penMinCode;

  /**
   * The Postal.
   */
  @Column(name = "POSTAL", length = 7)
  String postal;

  /**
   * The True pen.
   */
  @Column(name = "STUD_TRUE_NO", length = 10)
  String truePen;

  /**
   * The Grade.
   */
  @Column(name = "STUD_GRADE", length = 2)
  String grade;

  /**
   * The Grade year.
   */
  @Column(name = "STUD_GRADE_YEAR", length = 4)
  Integer gradeYear;

  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER_NAME", updatable = false, length = 15)
  String createUser;
  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;
  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER_NAME", length = 15)
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

  /**
   * The Merge to user name.
   */
  @Column(name = "MERGE_TO_USER_NAME", length = 15)
  String mergeToUserName;

  /**
   * The Merge to code.
   */
  @Column(name = "MERGE_TO_CODE", length = 2)
  String mergeToCode;

  /**
   * The Merge to date.
   */
  @Column(name = "MERGE_TO_DATE")
  LocalDateTime mergeToDate;

  /**
   * The Update demog date.
   */
  @Column(name = "UPDATE_DEMOG_DATE")
  LocalDateTime updateDemogDate;
}
