package ca.bc.gov.educ.api.pen.replication.rest;

import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.struct.EventOutcome;
import ca.bc.gov.educ.api.pen.replication.struct.EventType;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Instantiates a new Rest utils.
   *
   */
  @Autowired
  public RestUtils(final MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }


  @SneakyThrows(JsonProcessingException.class)
  @Retryable(value = {Exception.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Map<String, Student> getStudentsByID(final List<String> studentIDs) {
    log.info("called STUDENT_API to get students :: {}", studentIDs);
    final var event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(UUID.randomUUID()).eventType(EventType.GET_STUDENTS).eventPayload(JsonUtil.getJsonStringFromObject(studentIDs)).build();
    try {
      val responseEvent = JsonUtil.getJsonObjectFromByteArray(ca.bc.gov.educ.api.pen.replication.struct.Event.class, this.messagePublisher.requestMessage("STUDENT_API_TOPIC", JsonUtil.getJsonBytesFromObject(event)).get(2, TimeUnit.SECONDS).getData());
      log.info("got response from STUDENT_API  :: {}", responseEvent);
      if (responseEvent.getEventOutcome() == EventOutcome.STUDENT_NOT_FOUND) {
        log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
        throw new PenReplicationAPIRuntimeException("Student not found for , " + studentIDs);
      }
      final List<Student> students = this.objectMapper.readValue(responseEvent.getEventPayload(), new TypeReference<>() {
      });
      return students.stream().collect(Collectors.toConcurrentMap(Student::getStudentID, Function.identity()));
    } catch (final Exception e) {
      throw new PenReplicationAPIRuntimeException("Student not found for , " + studentIDs);
    }
  }
}
