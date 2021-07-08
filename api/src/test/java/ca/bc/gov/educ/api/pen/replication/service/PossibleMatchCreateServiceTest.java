package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The type Possible match create service test.
 */
public class PossibleMatchCreateServiceTest extends BasePenReplicationAPITest {
  private final String transactionID = "1234567890";
  private final String pen1 = "120164447";
  private final String pen2 = "120146667";
  private final String studentID1 = "a64dea18-7a2e-408a-adf4-a3117cfe049a";
  private final String studentID2 = "5315ea4b-55df-491a-a48a-57608eebb405";
  /**
   * The Rest utils.
   */
  @Autowired
  RestUtils restUtils;
  @Autowired
  private PossibleMatchCreateService possibleMatchCreateService;

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test process event given create possible match event and transaction part of saga should not save in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testProcessEvent_givenCREATE_POSSIBLE_MATCH_EventAndTransactionPartOfSaga_shouldNotSaveInDB() throws JsonProcessingException {
    when(this.restUtils.getStudentsByID(any())).thenReturn(this.createMockStudentMap());
    final PenTwinTransaction penTwinTransaction = PenTwinTransaction.builder().penTwin1(this.pen1).penTwin2(this.pen2).transactionStatus(TransactionStatus.IN_PROGRESS.getCode()).transactionID(this.transactionID).build();
    this.penReplicationTestUtils.getPenTwinTransactionRepository().save(penTwinTransaction);
    final var request = this.createPossibleMatch();
    final var event = TestUtils.createEvent(EventType.ADD_POSSIBLE_MATCH.toString(), request, this.penReplicationTestUtils.getEventRepository());
    this.possibleMatchCreateService.processEvent(request, event);
    final var penTwins = this.penReplicationTestUtils.getPenTwinsRepository().findAll();
    assertThat(penTwins).isEmpty();
    val updatedEvent = this.penReplicationTestUtils.getEventRepository().findByEventId(event.getEventId());
    assertThat(updatedEvent).isPresent();
    assertThat(updatedEvent.get().getEventStatus()).isEqualTo(PROCESSED.toString());
  }

  private List<PossibleMatch> createPossibleMatch() {
    final List<PossibleMatch> possibleMatches = new ArrayList<>();
    possibleMatches.add(PossibleMatch.builder().studentID(this.studentID1).matchedStudentID(this.studentID2).build());
    return possibleMatches;
  }

  private Map<String, Student> createMockStudentMap() {
    final Map<String, Student> studentMap = new HashMap<>();
    studentMap.put(this.studentID1, this.createStudentCreateRequest(this.pen1, this.studentID1));
    studentMap.put(this.studentID2, this.createStudentCreateRequest(this.pen2, this.studentID2));
    return studentMap;
  }

  /**
   * Create student create request student.
   *
   * @param pen       the pen
   * @param studentID the student id
   * @return the student
   */
  public Student createStudentCreateRequest(final String pen, final String studentID) {
    final Student student = new Student();
    student.setPen(pen);
    student.setStudentID(studentID);
    return student;
  }
}
