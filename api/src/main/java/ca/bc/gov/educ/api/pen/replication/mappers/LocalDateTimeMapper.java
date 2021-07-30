package ca.bc.gov.educ.api.pen.replication.mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * The type Local date time mapper.
 */
public class LocalDateTimeMapper {
  DateTimeFormatter formatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(ISO_LOCAL_TIME).toFormatter();

  /**
   * Map string.
   *
   * @param dateTime the date time
   * @return the string
   */
  public String map(final LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }

    return this.formatter.format(dateTime);
  }

  /**
   * Map local date time.
   *
   * @param dateTime the date time
   * @return the local date time
   */
  public LocalDateTime map(final String dateTime) {
    if (dateTime == null) {
      return null;
    }
    final DateTimeFormatter pattern;
    if (dateTime.contains("T")) {
      pattern = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss");
    } else {
      pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    return LocalDateTime.parse(dateTime.substring(0, 19), pattern);
  }

}
