package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * The type Student create service test.
 */
public class StudentCreateServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private PenDemogRepository penDemogRepository;
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private StudentCreateService studentCreateService;

  /**
   * Clean db.
   */
  @After
  public void cleanDB() {
    this.penDemogRepository.deleteAll();
    this.eventRepository.deleteAll();
  }

  /**
   * Test process event given create student event with null postal code should save blank postal code in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenCREATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    final var request = TestUtils.createStudentCreateRequest(null);
    final var event = TestUtils.createEvent("CREATE_STUDENT", request, this.eventRepository);
    this.eventRepository.save(event);
    this.studentCreateService.processEvent(request, event);
    final var penDemog = this.penDemogRepository.findById(request.getPen());
    assertThat(penDemog).isPresent();
    assertThat(penDemog.get().getPostalCode()).isEqualTo(" ");
  }
}
