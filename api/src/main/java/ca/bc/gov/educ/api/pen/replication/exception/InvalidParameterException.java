package ca.bc.gov.educ.api.pen.replication.exception;

import lombok.val;

/**
 * InvalidParameterException to provide error details when unexpected parameters are passed to endpoint
 *
 * @author John Cox
 */
public class InvalidParameterException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = -2325104800954988680L;

  /**
   * Instantiates a new Invalid parameter exception.
   *
   * @param searchParamsMap the search params map
   */
  public InvalidParameterException(final String... searchParamsMap) {
    super(InvalidParameterException.generateMessage(searchParamsMap));
  }

  /**
   * Generate message string.
   *
   * @param searchParams the search params
   * @return the string
   */
  private static String generateMessage(final String... searchParams) {
    val message = new StringBuilder("Unexpected request parameters provided: ");
    var prefix = "";
    for (final String parameter : searchParams) {
      message.append(prefix);
      prefix = ",";
      message.append(parameter);
    }
    return message.toString();
  }
}
