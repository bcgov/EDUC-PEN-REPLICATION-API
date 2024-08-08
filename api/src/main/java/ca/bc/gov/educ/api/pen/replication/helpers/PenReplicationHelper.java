package ca.bc.gov.educ.api.pen.replication.helpers;

import ca.bc.gov.educ.api.pen.replication.constants.MatchAndTwinReasonCode;
import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * The type Pen replication helper.
 */
@Slf4j
public final class PenReplicationHelper {

  private static final String DELETE_FROM_PEN_TWINS_WHERE_PEN_TWIN_1 = "delete from pen_twins where PEN_TWIN1 = '";
  private static final String AND_PEN_TWIN_2 = " AND PEN_TWIN2 = '";
  private static final String INSERT_INTO_PEN_TWINS = "insert into pen_twins (PEN_TWIN1, PEN_TWIN2, TWIN_REASON, RUN_DATE, TWIN_DATE, TWIN_USER_ID) values (";

  private PenReplicationHelper() {

  }

  public static PenDemographicsEntity getPenDemogFromStudentUpdate(final StudentUpdate studentUpdate, final PenDemographicsEntity existingPenDemog, final RestUtils restUtils) {
    val penDemog = PenDemogStudentMapper.mapper.toPenDemog(studentUpdate);
    penDemog.setCreateUser(studentUpdate.getCreateUser().substring(0,15));
    penDemog.setUpdateUser(studentUpdate.getUpdateUser().substring(0,15));
    penDemog.setUpdateDate(LocalDateTime.now());
    penDemog.setStudBirth(StringUtils.replace(penDemog.getStudBirth(), "-", ""));
    if (StringUtils.isNotBlank(studentUpdate.getTrueStudentID()) && StringUtils.isBlank(existingPenDemog.getStudentTrueNo())) {
      penDemog.setStudentTrueNo(restUtils.getStudentPen(studentUpdate.getTrueStudentID()));
      penDemog.setMergeToDate(LocalDate.now());
      penDemog.setMergeToCode("MI");
    } else if (StringUtils.isBlank(studentUpdate.getTrueStudentID()) && StringUtils.isNotBlank(existingPenDemog.getStudentTrueNo())) {
      penDemog.setStudentTrueNo(" ");
      penDemog.setMergeToDate(null);
      penDemog.setMergeToCode(" ");
    }
    return penDemog;
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
    return DELETE_FROM_PEN_TWINS_WHERE_PEN_TWIN_1
      + studentMap.get(possibleMatch.getStudentID()).getPen() + "'" +
      AND_PEN_TWIN_2 + studentMap.get(possibleMatch.getMatchedStudentID()).getPen() + "'";
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
    return INSERT_INTO_PEN_TWINS +
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
  public static String buildPenTwinInsertLeftSide(final PenTwinTransaction penTwinTransaction) {
    return INSERT_INTO_PEN_TWINS +
      "'" + penTwinTransaction.getPenTwin1() + "'" + "," +
      "'" + penTwinTransaction.getPenTwin2() + "'" + "," +
      "'" + penTwinTransaction.getTwinReason() + "'" + "," +
      "'" + penTwinTransaction.getRunDate() + "'" + "," +
      "'" + penTwinTransaction.getRunDate() + "'" + "," +
      "'" + penTwinTransaction.getTwinUserID() + "'" +
      ")";
  }

  /**
   * Build pen twin insert string.
   *
   * @param penTwinTransaction the pen twin transaction
   * @return the string
   */
  public static String buildPenTwinInsertRightSide(final PenTwinTransaction penTwinTransaction) {
    return INSERT_INTO_PEN_TWINS +
      "'" + penTwinTransaction.getPenTwin2() + "'" + "," +
      "'" + penTwinTransaction.getPenTwin1() + "'" + "," +
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
  public static String buildPenTwinDeleteLeftSide(final PenTwinTransaction penTwinTransaction) {
    return DELETE_FROM_PEN_TWINS_WHERE_PEN_TWIN_1
      + penTwinTransaction.getPenTwin1() + "'" +
      AND_PEN_TWIN_2 + penTwinTransaction.getPenTwin2() + "'";
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

  /**
   * Build pen twin delete right side string.
   *
   * @param penTwinTransaction the pen twin transaction
   * @return the string
   */
  public static String buildPenTwinDeleteRightSide(final PenTwinTransaction penTwinTransaction) {
    return DELETE_FROM_PEN_TWINS_WHERE_PEN_TWIN_1
      + penTwinTransaction.getPenTwin2() + "'" +
      AND_PEN_TWIN_2 + penTwinTransaction.getPenTwin1() + "'";
  }
}
