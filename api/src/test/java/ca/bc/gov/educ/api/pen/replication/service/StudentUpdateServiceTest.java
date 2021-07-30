package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * The type Student update service test.
 */
public class StudentUpdateServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private StudentCreateService studentCreateService;
  @Autowired
  private StudentUpdateService studentUpdateService;


  /**
   * Sets up.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Before
  public void setUp() throws JsonProcessingException {
    final var request = TestUtils.createStudentCreateRequest("V8V2P8");
    final var event = TestUtils.createEvent("CREATE_STUDENT", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.studentCreateService.processEvent(request, event);
  }

  /**
   * Test process event given update student event with null postal code should save blank postal code in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenUPDATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    final var request = this.createStudentUpdateRequest();
    final var event = TestUtils.createEvent("UPDATE_STUDENT", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.studentUpdateService.processEvent(request, event);
    final var penDemog = this.penReplicationTestUtils.getPenDemogRepository().findById(request.getPen().trim());
    assertThat(penDemog).isPresent();
  }

  /**
   * Test process event given update student event and transaction part of saga should not save in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenUPDATE_STUDENT_EventAndTransactionPartOfSaga_shouldNotSaveInDB() throws JsonProcessingException {
    final PenDemogTransaction penDemogTransaction = PenDemogTransaction.builder().pen("987654321").transactionStatus(TransactionStatus.IN_PROGRESS.getCode()).transactionID("1234567890").build();
    this.penReplicationTestUtils.getPenDemogTransactionRepository().save(penDemogTransaction);
    final var request = this.createStudentUpdateRequest();
    final var event = TestUtils.createEvent("UPDATE_STUDENT", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.studentUpdateService.processEvent(request, event);
    final var penDemog = this.penReplicationTestUtils.getPenDemogRepository().findById(request.getPen().trim());
    assertThat(penDemog).isPresent();
    assertThat(penDemog.get().getPostalCode()).isEqualTo("V8V2P8");
    val updatedEvent = this.penReplicationTestUtils.getEventRepository().findByEventId(event.getEventId());
    assertThat(updatedEvent).isPresent();
    assertThat(updatedEvent.get().getEventStatus()).isEqualTo(PROCESSED.toString());
  }

  private StudentUpdate createStudentUpdateRequest() {
    final StudentUpdate student = new StudentUpdate();
    TestUtils.initializeBaseStudentRequest(student);
    student.setHistoryActivityCode("USEREDIT");
    return student;
  }
}
