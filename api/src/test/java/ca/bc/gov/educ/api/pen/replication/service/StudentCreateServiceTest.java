package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * The type Student create service test.
 */
public class StudentCreateServiceTest extends BasePenReplicationAPITest {


  @Autowired
  private StudentCreateService studentCreateService;

  /**
   * Test process event given create student event with null postal code should save blank postal code in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenCREATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    final var request = TestUtils.createStudentCreateRequest(null);
    final var event = TestUtils.createEvent("CREATE_STUDENT", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.studentCreateService.processEvent(request, event);
    final var penDemog = this.penReplicationTestUtils.getPenDemogRepository().findById(request.getPen().trim());
    assertThat(penDemog).isPresent();
  }


  /**
   * Test process event given create student event with null postal code should save blank postal code in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenCREATE_STUDENT_EventAndTransactionPartOfSaga_shouldNotSaveInDB() throws JsonProcessingException {
    final PenDemogTransaction penDemogTransaction = PenDemogTransaction.builder().pen("987654321").updateDate(LocalDateTime.now()).transactionStatus(TransactionStatus.IN_PROGRESS.getCode()).transactionID("1234567890").build();
    this.penReplicationTestUtils.getPenDemogTransactionRepository().save(penDemogTransaction);
    final var request = TestUtils.createStudentCreateRequest(null);
    final var event = TestUtils.createEvent("CREATE_STUDENT", request, this.penReplicationTestUtils.getEventRepository());
    this.studentCreateService.processEvent(request, event);
    final var penDemog = this.penReplicationTestUtils.getPenDemogRepository().findById(request.getPen().trim());
    assertThat(penDemog).isNotPresent();
    val updatedEvent = this.penReplicationTestUtils.getEventRepository().findByEventId(event.getEventId());
    assertThat(updatedEvent).isPresent();
    assertThat(updatedEvent.get().getEventStatus()).isEqualTo(PROCESSED.toString());
  }
}
