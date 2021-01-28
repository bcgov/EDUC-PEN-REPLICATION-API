package ca.bc.gov.educ.api.pen.replication.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The type Json util.
 */
public final class JsonUtil {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Instantiates a new Json util.
   */
  private JsonUtil() {
  }

  /**
   * Gets json string from object.
   *
   * @param payload the payload
   * @return the json string from object
   * @throws JsonProcessingException the json processing exception
   */
  public static String getJsonStringFromObject(Object payload) throws JsonProcessingException {
    return objectMapper.writeValueAsString(payload);
  }

  /**
   * Gets json object from string.
   *
   * @param <T>     the type parameter
   * @param clazz   the clazz
   * @param payload the payload
   * @return the json object from string
   * @throws JsonProcessingException the json processing exception
   */
  public static <T> T getJsonObjectFromString(Class<T> clazz, String payload) throws JsonProcessingException {
    return objectMapper.readValue(payload, clazz);
  }

  /**
   * Gets json pretty string from object.
   *
   * @param payload the payload
   * @return the json pretty string from object
   */
  public static String getJsonPrettyStringFromObject(Object payload) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  /**
   * Get json bytes from object byte [ ].
   *
   * @param payload the payload
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  public static byte[] getJsonBytesFromObject(Object payload) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsBytes(payload);
  }
}
