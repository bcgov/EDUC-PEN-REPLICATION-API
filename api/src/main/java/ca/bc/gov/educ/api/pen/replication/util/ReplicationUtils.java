package ca.bc.gov.educ.api.pen.replication.util;

import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * The type Replication utils.
 */
public final class ReplicationUtils {
  private static final LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();
  private ReplicationUtils() {

  }

  /**
   * Get blank when null string.
   *
   * @param s the s
   * @return the string
   */
  public static String getBlankWhenNull(final String s) {
    if (StringUtils.isNotEmpty(s)) {
      return s;
    }
    //Return a blank to PEN_DEMOG in these cases as per our reqs
    return " ";
  }

  /**
   * Translates the GRAD_STUDENT_RECORDS status to the STUDENT_MASTER ARCHIVE_FLAG.
   *
   * @param gradStudentStatus the GRAD_STUDENT_RECORDS student status
   * @return "A" for CUR or TER, otherwise "I"
   */
  public static String getTraxArchiveFlag(final String gradStudentStatus) {
    if (gradStudentStatus.equalsIgnoreCase("CUR") || gradStudentStatus.equalsIgnoreCase("TER")) {
      return "A";
    }
    return "I";
  }

  /**
   * Translates the GRAD_STUDENT_RECORDS status to the STUDENT_MASTER STUD_STATUS.
   *
   * @param gradStudentStatus the GRAD_STUDENT_RECORDS student status
   * @return "A" for CUR/ARC, "D" for DEC, "M" for MER, "T" for TER, otherwise "A"
   */
  public static String getTraxStudentStatus(final String gradStudentStatus) {
    if (gradStudentStatus.equalsIgnoreCase("CUR") || gradStudentStatus.equalsIgnoreCase("ARC")) {
      return "A";
    } else if (gradStudentStatus.equalsIgnoreCase("DEC")) {
      return "D";
    } else if (gradStudentStatus.equalsIgnoreCase("MER")) {
      return "M";
    } else if (gradStudentStatus.equalsIgnoreCase("TER")) {
      return "T";
    }
    return "A";
  }

  public static void setCloseDateIfRequired(School school, SchoolMasterEntity entity){
    if (StringUtils.isNotEmpty(school.getClosedDate()) && dateTimeMapper.map(school.getClosedDate()).isAfter(LocalDateTime.now())){
      if(entity != null) {
        school.setClosedDate(dateTimeMapper.map(entity.getDateClosed()));
      }else{
        school.setClosedDate(null);
      }
    }
  }

  public static void setCloseDateIfRequired(IndependentAuthority authority, AuthorityMasterEntity entity){
    if (StringUtils.isNotEmpty(authority.getClosedDate()) && dateTimeMapper.map(authority.getClosedDate()).isAfter(LocalDateTime.now())){
      if(entity != null) {
        authority.setClosedDate(dateTimeMapper.map(entity.getDateClosed()));
      }else{
        authority.setClosedDate(null);
      }
    }
  }
}
