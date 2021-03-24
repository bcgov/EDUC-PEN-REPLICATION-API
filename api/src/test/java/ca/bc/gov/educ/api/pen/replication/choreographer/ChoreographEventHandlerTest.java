package ca.bc.gov.educ.api.pen.replication.choreographer;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.struct.EventOutcome;
import ca.bc.gov.educ.api.pen.replication.struct.EventType;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class ChoreographEventHandlerTest {

  @Autowired
  ChoreographEventHandler choreographEventHandler;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHandleEvent_givenEventTypeStudentCreate_shouldWriteToPenDemog() throws JsonProcessingException {
    this.choreographEventHandler.handleEvent(this.buildEvent());
  }

  private Event buildEvent() throws JsonProcessingException {
    return Event.builder().eventType(EventType.CREATE_STUDENT.toString())
        .eventOutcome(EventOutcome.STUDENT_CREATED.toString())
        .penReplicationEventId(UUID.randomUUID())
        .createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now())
        .eventPayload(JsonUtil.getJsonStringFromObject(this.createStudent()))
        .build();
  }

  private StudentCreate createStudent() {
    final StudentCreate student = new StudentCreate();
    student.setPen("987654321");
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
    return student;
  }
}
