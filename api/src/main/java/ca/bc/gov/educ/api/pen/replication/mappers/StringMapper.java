package ca.bc.gov.educ.api.pen.replication.mappers;

import org.apache.commons.lang3.StringUtils;

/**
 * The type String mapper.
 */
public class StringMapper {

  /**
   * Map string.
   *
   * @param value the value
   * @return the string
   */
  public String map(String value) {
    if (StringUtils.isNotBlank(value)) {
      return value.trim().toUpperCase();
    }
    return " ";
  }
}
