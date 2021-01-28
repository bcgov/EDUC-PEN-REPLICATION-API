package ca.bc.gov.educ.api.pen.replication.mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The type Local date time mapper.
 */
public class LocalDateTimeMapper {

  /**
   * Map string.
   *
   * @param dateTime the date time
   * @return the string
   */
  public String map(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
  }

  /**
   * Map local date time.
   *
   * @param dateTime the date time
   * @return the local date time
   */
  public LocalDateTime map(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return LocalDateTime.parse(dateTime, pattern);
  }

}
