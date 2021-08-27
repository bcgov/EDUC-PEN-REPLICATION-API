package ca.bc.gov.educ.api.pen.replication.rest;

import ca.bc.gov.educ.api.pen.replication.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  private static final String STUDENT_NOT_FOUND_FOR = "Student not found for , ";
  private static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Instantiates a new Rest utils.
   *
   * @param messagePublisher the message publisher
   */
  @Autowired
  public RestUtils(final MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }


  /**
   * Gets students by id.
   *
   * @param studentIDs the student i ds
   * @return the students by id
   */
  @SneakyThrows({IOException.class, InterruptedException.class})
  @Retryable(value = {Exception.class}, exclude = {PenReplicationAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Map<String, Student> getStudentsByID(final List<String> studentIDs) {
    log.info("called STUDENT_API to get students :: {}", studentIDs);
    final var event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(UUID.randomUUID()).eventType(EventType.GET_STUDENTS).eventPayload(JsonUtil.getJsonStringFromObject(studentIDs)).build();
    try {
      val response = this.messagePublisher.requestMessage(STUDENT_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 5, TimeUnit.SECONDS).get();
      if (response == null || response.getData() == null || response.getData().length == 0) {
        log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
        throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs);
      }
      val responseEvent = JsonUtil.getJsonObjectFromByteArray(ca.bc.gov.educ.api.pen.replication.struct.Event.class, response.getData());
      log.info("got response from STUDENT_API  :: {}", responseEvent);
      if (responseEvent.getEventOutcome() == EventOutcome.STUDENTS_NOT_FOUND) {
        log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
        throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs);
      }
      final List<Student> students = this.objectMapper.readValue(responseEvent.getEventPayload(), new TypeReference<>() {
      });
      log.info("got response from STUDENT_API found :: {} students", students.size());
      return students.stream().collect(Collectors.toConcurrentMap(Student::getStudentID, Function.identity()));
    } catch (final ExecutionException e) {
      throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs + " :: " + e.getMessage());
    }
  }

  /**
   * Gets student pen.
   *
   * @param trueStudentID the true student id
   * @return the student pen
   */
  public String getStudentPen(final String trueStudentID) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(trueStudentID);
    return this.getStudentsByID(studentIDs).get(trueStudentID).getPen();
  }

  /**
   * Create student map from possible match map.
   *
   * @param possibleMatch the possible match
   * @return the map
   */
  public Map<String, Student> createStudentMapFromPossibleMatch(final PossibleMatch possibleMatch) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(possibleMatch.getStudentID());
    studentIDs.add(possibleMatch.getMatchedStudentID());
    return this.getStudentsByID(studentIDs);
  }


  /**
   * Create student map from pen numbers map.
   *
   * @param pens   the pens
   * @param sagaId the saga id
   * @return the map
   */
  @SneakyThrows
  @Retryable(value = {Exception.class}, exclude = {PenReplicationAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Map<String, Student> createStudentMapFromPenNumbers(@NonNull final List<String> pens, final UUID sagaId) {
    final Map<String, Student> penStudentMap = new HashMap<>();
    for (val pen : pens) {
      val event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(sagaId == null ? UUID.randomUUID() : sagaId).eventType(EventType.GET_STUDENT).eventPayload(pen).build();
      val response = this.messagePublisher.requestMessage(STUDENT_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 5, TimeUnit.SECONDS).get();
      if (response == null) {
        throw new PenReplicationAPIRuntimeException("Either NATS is down or api is down as response could not be received.");
      }
      if (response.getData() != null && response.getData().length > 0) {
        val student = JsonUtil.getJsonObjectFromByteArray(Student.class, response.getData());
        penStudentMap.put(pen, student);
      }
    }
    return penStudentMap;
  }


}
