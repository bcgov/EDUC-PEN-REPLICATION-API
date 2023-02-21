package ca.bc.gov.educ.api.pen.replication.util;

import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
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

  public static void setCloseDateIfRequired(School school, SchoolMasterEntity entity){
    if (StringUtils.isNotEmpty(school.getClosedDate()) && dateTimeMapper.map(school.getClosedDate()).isAfter(LocalDateTime.now())){
      if(entity != null) {
        school.setClosedDate(entity.getClosedDate());
      }else{
        school.setClosedDate(null);
      }
    }
  }
}
