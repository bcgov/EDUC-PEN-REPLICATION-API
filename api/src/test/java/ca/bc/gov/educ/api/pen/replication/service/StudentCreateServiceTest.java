package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.PenReplicationApiResourceApplication;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PenReplicationApiResourceApplication.class})
@ActiveProfiles("test")
public class StudentCreateServiceTest {

  @Autowired
  private PenDemogRepository penDemogRepository;
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private StudentCreateService studentCreateService;

  @After
  public void cleanDB() {
    this.penDemogRepository.deleteAll();
    this.eventRepository.deleteAll();
  }

  @Test
  public void testProcessEvent_givenCREATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    var request = TestUtils.createStudentCreateRequest(null);
    var event = TestUtils.createEvent("CREATE_STUDENT", request, eventRepository);
    eventRepository.save(event);
    studentCreateService.processEvent(request, event);
    var penDemog = penDemogRepository.findById(request.getPen());
    assertThat(penDemog).isPresent();
    assertThat(penDemog.get().getPostalCode()).isEqualTo(" ");
  }
}
