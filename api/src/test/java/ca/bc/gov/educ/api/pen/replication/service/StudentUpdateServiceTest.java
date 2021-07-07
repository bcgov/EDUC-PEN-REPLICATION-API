package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * The type Student update service test.
 */
public class StudentUpdateServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private PenDemogRepository penDemogRepository;
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private StudentCreateService studentCreateService;
  @Autowired
  private StudentUpdateService studentUpdateService;

  /**
   * Clean db.
   */
  @After
  public void cleanDB() {
    this.penDemogRepository.deleteAll();
    this.eventRepository.deleteAll();
  }

  /**
   * Sets up.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Before
  public void setUp() throws JsonProcessingException {
    var request = TestUtils.createStudentCreateRequest("V8V2P8");
    var event = TestUtils.createEvent("CREATE_STUDENT", request, eventRepository);
    eventRepository.save(event);
    studentCreateService.processEvent(request, event);
  }

  /**
   * Test process event given update student event with null postal code should save blank postal code in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenUPDATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    var request = createStudentUpdateRequest();
    var event = TestUtils.createEvent("UPDATE_STUDENT", request, eventRepository);
    eventRepository.save(event);
    studentUpdateService.processEvent(request, event);
    var penDemog = penDemogRepository.findById(request.getPen());
    assertThat(penDemog).isPresent();
    assertThat(penDemog.get().getPostalCode()).isEqualTo(" ");
  }

  private StudentUpdate createStudentUpdateRequest() {
    StudentUpdate student = new StudentUpdate();
    TestUtils.initializeBaseStudentRequest(student);
    student.setHistoryActivityCode("USEREDIT");
    return student;
  }
}
