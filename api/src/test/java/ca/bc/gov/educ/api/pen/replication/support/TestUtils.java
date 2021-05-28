package ca.bc.gov.educ.api.pen.replication.support;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.BaseStudent;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;

public class TestUtils {
  public static void initializeBaseStudentRequest(BaseStudent student) {
    student.setStudentID(UUID.randomUUID().toString());
    student.setPen("987654321 ");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob("1907-05-26");
    student.setSexCode("M");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setEmailVerified("Y");
    student.setDeceasedDate("1979-06-11");
    student.setCreateDate("2021-04-23 15:13:45");
    student.setUpdateDate("2021-04-23 15:13:45");
  }

  public static StudentCreate createStudentCreateRequest(String postalCode) {
    StudentCreate student = new StudentCreate();
    TestUtils.initializeBaseStudentRequest(student);
    student.setPostalCode(postalCode);
    student.setHistoryActivityCode("USERNEW");
    return student;
  }

  public static Event createEvent(String eventType, Object payload, EventRepository eventRepository) throws JsonProcessingException {
    var event = Event.builder()
      .eventType(eventType)
      .eventId(UUID.randomUUID())
      .eventOutcome("PROCESSED")
      .eventPayload(JsonUtil.getJsonStringFromObject(payload))
      .eventStatus(DB_COMMITTED.toString())
      .createUser("PEN-REPLICATION-API")
      .updateUser("PEN-REPLICATION-API")
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
    eventRepository.save(event);
    return event;
  }
}
