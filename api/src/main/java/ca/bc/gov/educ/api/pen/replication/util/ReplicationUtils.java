package ca.bc.gov.educ.api.pen.replication.util;

import org.apache.commons.lang3.StringUtils;

/**
 * The type Replication utils.
 */
public final class ReplicationUtils {

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
}
