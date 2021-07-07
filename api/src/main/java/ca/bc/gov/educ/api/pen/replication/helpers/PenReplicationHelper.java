package ca.bc.gov.educ.api.pen.replication.helpers;

import ca.bc.gov.educ.api.pen.replication.constants.MatchAndTwinReasonCode;
import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.util.ReplicationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * The type Pen replication helper.
 */
@Slf4j
public final class PenReplicationHelper {

  private static final String TO_DATE = "TO_DATE('";
  private static final String YYYY_MM_DD_HH_24_MI_SS = ", 'YYYY-MM-DD HH24:MI:SS'),";

  private PenReplicationHelper() {

  }

  /**
   * Build pen demog insert string.
   *
   * @param studentCreate the student create
   * @return the string
   */
  public static String buildPenDemogInsert(final StudentCreate studentCreate) {
    val penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentCreate);
    penDemographicsEntity.setCreateDate(formatDateTime(penDemographicsEntity.getCreateDate()));
    penDemographicsEntity.setUpdateDate(formatDateTime(penDemographicsEntity.getUpdateDate()));
    penDemographicsEntity.setStudBirth(StringUtils.replace(penDemographicsEntity.getStudBirth(), "-", ""));

    final String insert = "insert into pen_demog (create_date, create_user_name, stud_demog_code, stud_grade, stud_grade_year, pen_local_id, merge_to_code, merge_to_date, merge_to_user_name, pen_mincode, postal, stud_birth, stud_given, stud_middle, stud_sex, stud_status, stud_surname, stud_true_no, update_date, update_user_name, usual_given, usual_middle, usual_surname, stud_no) values (" +
      TO_DATE + penDemographicsEntity.getCreateDate() + "'" + YYYY_MM_DD_HH_24_MI_SS +
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
      TO_DATE + penDemographicsEntity.getUpdateDate() + "'" + YYYY_MM_DD_HH_24_MI_SS +
      "'" + penDemographicsEntity.getUpdateUser() + "'" + "," +
      "'" + (penDemographicsEntity.getUsualGiven() == null ? "" : penDemographicsEntity.getUsualGiven()) + "'" + "," +
      "'" + (penDemographicsEntity.getUsualMiddle() == null ? "" : penDemographicsEntity.getUsualMiddle()) + "'" + "," +
      "'" + (penDemographicsEntity.getUsualSurname() == null ? "" : penDemographicsEntity.getUsualSurname()) + "'" + "," +
      "'" + penDemographicsEntity.getStudNo() + "'" +
      ")";
    log.debug("Pen Demog Insert statement: " + insert);
    return insert;
  }

  /**
   * Build pen demog update string.
   *
   * @param studentUpdate    the student update
   * @param existingPenDemog the existing pen demog
   * @param restUtils        the rest utils
   * @return the string
   */
  public static String buildPenDemogUpdate(final StudentUpdate studentUpdate, final PenDemographicsEntity existingPenDemog, final RestUtils restUtils) {
    val penDemographicsEntity = getPenDemogFromStudentUpdate(studentUpdate, existingPenDemog, restUtils);
    final String updatePenDemogStatement = "UPDATE PEN_DEMOG SET "
      + "STUD_DEMOG_CODE=" + "'" + (penDemographicsEntity.getDemogCode() == null ? "" : penDemographicsEntity.getDemogCode()) + "'" + ","
      + "STUD_GRADE=" + "'" + ReplicationUtils.getBlankWhenNull(penDemographicsEntity.getGrade()) + "'" + ","
      + "STUD_GRADE_YEAR=" + "'" + (penDemographicsEntity.getGradeYear() == null ? "" : penDemographicsEntity.getGradeYear()) + "'" + ","
      + "PEN_LOCAL_ID=" + "'" + (penDemographicsEntity.getLocalID() == null ? "" : penDemographicsEntity.getLocalID()) + "'" + ","
      + "PEN_MINCODE=" + "'" + (penDemographicsEntity.getMincode() == null ? "" : penDemographicsEntity.getMincode()) + "'" + ","
      + "POSTAL=" + "'" + (penDemographicsEntity.getPostalCode() == null ? " " : penDemographicsEntity.getPostalCode()) + "'" + ","
      + "STUD_BIRTH=" + "'" + penDemographicsEntity.getStudBirth() + "'" + ","
      + "STUD_GIVEN=" + "'" + (penDemographicsEntity.getStudGiven() == null ? "" : penDemographicsEntity.getStudGiven()) + "'" + ","
      + "STUD_MIDDLE=" + "'" + (penDemographicsEntity.getStudMiddle() == null ? "" : penDemographicsEntity.getStudMiddle()) + "'" + ","
      + "STUD_SEX=" + "'" + penDemographicsEntity.getStudSex() + "'" + ","
      + "STUD_STATUS=" + "'" + (penDemographicsEntity.getStudStatus() == null ? "" : penDemographicsEntity.getStudStatus()) + "'" + ","
      + "STUD_SURNAME=" + "'" + penDemographicsEntity.getStudSurname() + "'" + ","
      + "MERGE_TO_DATE=" + getMergeToDate(penDemographicsEntity.getMergeToDate()) + ","
      + "MERGE_TO_CODE=" + "'" + (penDemographicsEntity.getMergeToCode() == null ? "" : penDemographicsEntity.getMergeToCode()) + "'" + ","
      + "STUD_TRUE_NO=" + "'" + (penDemographicsEntity.getStudentTrueNo() == null ? "" : penDemographicsEntity.getStudentTrueNo()) + "'" + ","
      + "UPDATE_DATE=" + TO_DATE + penDemographicsEntity.getUpdateDate() + "'" + YYYY_MM_DD_HH_24_MI_SS
      + "UPDATE_USER_NAME=" + "'" + penDemographicsEntity.getUpdateUser() + "'" + ","
      + "USUAL_GIVEN=" + "'" + (penDemographicsEntity.getUsualGiven() == null ? "" : penDemographicsEntity.getUsualGiven()) + "'" + ","
      + "USUAL_MIDDLE=" + "'" + (penDemographicsEntity.getUsualMiddle() == null ? "" : penDemographicsEntity.getUsualMiddle()) + "'" + ","
      + "USUAL_SURNAME=" + "'" + (penDemographicsEntity.getUsualSurname() == null ? "" : penDemographicsEntity.getUsualSurname()) + "'"
      + " WHERE STUD_NO=" + "'" + StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10) + "'"; // a space is appended CAREFUL not to remove.
    log.debug("Update Pen Demog: " + updatePenDemogStatement);
    return updatePenDemogStatement;
  }

  private static PenDemographicsEntity getPenDemogFromStudentUpdate(final StudentUpdate studentUpdate, final PenDemographicsEntity existingPenDemog, final RestUtils restUtils) {
    val penDemog = PenDemogStudentMapper.mapper.toPenDemog(studentUpdate);
    penDemog.setCreateDate(formatDateTime(penDemog.getCreateDate()));
    penDemog.setUpdateDate(formatDateTime(penDemog.getUpdateDate()));
    penDemog.setStudBirth(StringUtils.replace(penDemog.getStudBirth(), "-", ""));
    if (StringUtils.isNotBlank(studentUpdate.getTrueStudentID()) && StringUtils.isBlank(existingPenDemog.getStudentTrueNo())) {
      penDemog.setStudentTrueNo(restUtils.getStudentPen(studentUpdate.getTrueStudentID()));
      penDemog.setMergeToDate(studentUpdate.getUpdateDate());
      penDemog.setMergeToCode("MI");
    } else if (StringUtils.isBlank(studentUpdate.getTrueStudentID()) && StringUtils.isNotBlank(existingPenDemog.getStudentTrueNo())) {
      penDemog.setStudentTrueNo(null);
      penDemog.setMergeToDate(null);
      penDemog.setMergeToCode(null);
    }
    return penDemog;
  }

  /**
   * Format date time string.
   *
   * @param activityDate the activity date
   * @return the string
   */
  public static String formatDateTime(String activityDate) {
    if (StringUtils.isBlank(activityDate)) {
      return activityDate;
    }
    activityDate = activityDate.replace("T", " ");
    if (activityDate.length() > 19) {
      activityDate = activityDate.substring(0, 19);
    }
    return activityDate;
  }

  /**
   * Gets merge to date.
   *
   * @param mergeToDate the merge to date
   * @return the merge to date
   */
  public static String getMergeToDate(final String mergeToDate) {
    if (mergeToDate == null) {
      return "''";
    }
    return TO_DATE + mergeToDate.substring(0, 19).replace("T", " ") + "'" + ", 'YYYY-MM-DD HH24:MI:SS')";
  }

  /**
   * Build pen twin delete string.
   *
   * @param possibleMatch the possible match
   * @param restUtils     the rest utils
   * @return the string
   */
  public static String buildPenTwinDelete(final PossibleMatch possibleMatch, final RestUtils restUtils) {
    val studentMap = restUtils.createStudentMapFromPossibleMatch(possibleMatch);
    return "delete from pen_twins where PEN_TWIN1 = '"
      + studentMap.get(possibleMatch.getStudentID()).getPen() + "'" +
      " AND PEN_TWIN2 = '" + studentMap.get(possibleMatch.getMatchedStudentID()).getPen() + "'";
  }

  /**
   * Build pen twin insert string.
   *
   * @param possibleMatch the possible match
   * @param restUtils     the rest utils
   * @return the string
   */
  public static String buildPenTwinInsert(final PossibleMatch possibleMatch, final RestUtils restUtils) {
    final Map<String, Student> studentMap = restUtils.createStudentMapFromPossibleMatch(possibleMatch);
    return "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (" +
      "'" + studentMap.get(possibleMatch.getStudentID()).getPen() + "'" + "," +
      "'" + studentMap.get(possibleMatch.getMatchedStudentID()).getPen() + "'" + "," +
      "'" + findByPrrMatchCode(possibleMatch.getMatchReasonCode()).getOldCode() + "'" + "," +
      "'" + possibleMatch.getCreateDate().substring(0, 10).replace("-", "") + "'" + "," +
      "'" + possibleMatch.getCreateDate().substring(0, 10).replace("-", "") + "'" + "," +
      "'" + possibleMatch.getCreateUser() + "'" +
      ")";
  }

  /**
   * Build pen twin insert string.
   *
   * @param penTwinTransaction the pen twin transaction
   * @return the string
   */
  public static String buildPenTwinInsert(final PenTwinTransaction penTwinTransaction) {
    return "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (" +
      "'" + penTwinTransaction.getPenTwin1() + "'" + "," +
      "'" + penTwinTransaction.getPenTwin2() + "'" + "," +
      "'" + penTwinTransaction.getTwinReason() + "'" + "," +
      "'" + penTwinTransaction.getRunDate() + "'" + "," +
      "'" + penTwinTransaction.getRunDate() + "'" + "," +
      "'" + penTwinTransaction.getTwinUserID() + "'" +
      ")";
  }

  /**
   * Build pen twin delete string.
   *
   * @param penTwinTransaction the pen twin transaction
   * @return the string
   */
  public static String buildPenTwinDelete(final PenTwinTransaction penTwinTransaction) {
    return "delete from pen_twins where PEN_TWIN1 = '"
      + penTwinTransaction.getPenTwin1() + "'" +
      " AND PEN_TWIN2 = '" + penTwinTransaction.getPenTwin2() + "'";
  }


  /**
   * Find by prr match code match and twin reason code.
   *
   * @param matchCode the match code
   * @return the match and twin reason code
   */
  public static MatchAndTwinReasonCode findByPrrMatchCode(final MatchReasonCodes matchCode) {
    if (matchCode == null) {
      return MatchAndTwinReasonCode.AU;
    }
    return Arrays.stream(MatchAndTwinReasonCode.values()).filter(value -> value.getPrrCode().equals(matchCode.toString())).findFirst().orElse(MatchAndTwinReasonCode.AU);
  }

  /**
   * Find match reason by old match code match reason codes.
   *
   * @param oldMatchCode the old match code
   * @return the match reason codes
   */
  public static MatchReasonCodes findMatchReasonByOldMatchCode(final String oldMatchCode) {
    if (oldMatchCode == null) {
      return MatchReasonCodes.MINISTRY;
    }
    return Arrays.stream(MatchReasonCodes.values()).filter(value -> value.toString().equals(oldMatchCode)).findFirst().orElse(MatchReasonCodes.MINISTRY);
  }
}
