package ca.bc.gov.educ.api.pen.replication.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

/**
 * The type Json util.
 */
@Slf4j
public final class JsonUtil {
  /**
   * The constant objectMapper.
   */
  public static final ObjectMapper objectMapper = new ObjectMapper();

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
  public static String getJsonStringFromObject(final Object payload) throws JsonProcessingException {
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
  public static <T> T getJsonObjectFromString(final Class<T> clazz, final String payload) throws JsonProcessingException {
    return objectMapper.readValue(payload, clazz);
  }

  /**
   * Gets json pretty string from object.
   *
   * @param payload the payload
   * @return the json pretty string from object
   */
  public static String getJsonPrettyStringFromObject(final Object payload) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    } catch (final JsonProcessingException e) {
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
  public static byte[] getJsonBytesFromObject(final Object payload) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsBytes(payload);
  }

  /**
   * Gets json object from byte array.
   *
   * @param <T>     the type parameter
   * @param clazz   the clazz
   * @param payload the payload
   * @return the json object from byte array
   * @throws IOException the io exception
   */
  public static <T> T getJsonObjectFromByteArray(final Class<T> clazz, final byte[] payload) throws IOException {
    return objectMapper.readValue(payload, clazz);
  }

  /**
   * Get json string optional.
   *
   * @param payload the payload
   * @return the optional
   */
  public static Optional<String> getJsonString(Object payload) {
    try {
      return Optional.ofNullable(objectMapper.writeValueAsString(payload));
    } catch (final Exception ex) {
      log.error("Exception while converting object to JSON String :: {}", payload);
    }
    return Optional.empty();
  }
}
