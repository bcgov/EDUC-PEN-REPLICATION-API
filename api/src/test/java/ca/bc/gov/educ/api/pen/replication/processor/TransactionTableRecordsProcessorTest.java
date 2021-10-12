package ca.bc.gov.educ.api.pen.replication.processor;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionType;
import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.Student;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The type Transaction table records processor test.
 */
public class TransactionTableRecordsProcessorTest extends BasePenReplicationAPITest {
  private final String transactionID = "1234567890";
  private final String pen1 = "120164447";
  private final String pen2 = "120146667";

  @Autowired
  RestUtils restUtils;
  @Autowired
  private TransactionTableRecordsProcessor transactionTableRecordsProcessor;

  /**
   * Sets up.
   *
   * @throws IOException the io exception
   */
  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    this.penReplicationTestUtils.getPenTwinTransactionRepository().save(this.createMockPenTwinTransaction());
    this.penReplicationTestUtils.getPenDemogTransactionRepository().save(this.createMockPenDemogTransaction());
  }


  /**
   * Test process unprocessed records given pending records in db should start processing and mark them in progress.
   */
  @Test
  public void testProcessUnprocessedRecords_givenPendingRecordsInDB_shouldStartProcessingAndMarkThemInProgress() {
    when(restUtils.createStudentMapFromPenNumbers(any(), any())).thenReturn(mockStudentsMap());
    this.transactionTableRecordsProcessor.processUnprocessedRecords();
    val results = this.penReplicationTestUtils.getSagaRepository().findAll();
    assertThat(results).isNotEmpty();
    assertThat(results.size()).isEqualTo(2);
    val penDemogTr = this.penReplicationTestUtils.getPenDemogTransactionRepository().findById(this.transactionID);
    assertThat(penDemogTr).isPresent();
    assertThat(penDemogTr.get().getTransactionStatus()).isEqualTo(TransactionStatus.IN_PROGRESS.getCode());
    val penTwinTr = this.penReplicationTestUtils.getPenTwinTransactionRepository().findById(this.transactionID);
    assertThat(penTwinTr).isPresent();
    assertThat(penTwinTr.get().getTransactionStatus()).isEqualTo(TransactionStatus.IN_PROGRESS.getCode());

  }

  private Map<String, Student> mockStudentsMap() {
    Map<String, Student> studentMap = new HashMap<>();
    studentMap.put(this.pen1, Student.builder().studentID(UUID.randomUUID().toString()).build());
    studentMap.put(this.pen2, Student.builder().studentID(UUID.randomUUID().toString()).build());
    return studentMap;
  }

  private PenTwinTransaction createMockPenTwinTransaction() {
    return PenTwinTransaction.builder()
      .transactionID(this.transactionID)
      .penTwin1(this.pen1)
      .penTwin2(this.pen2)
      .transactionType(TransactionType.DELETE_TWINS.getCode())
      .transactionInsertDateTime(LocalDateTime.now())
      .twinReason("MI")
      .twinUserID("test-user")
      .transactionStatus(TransactionStatus.PENDING.getCode())
      .runDate("20210101")
      .build();
  }

  /**
   * Create mock pen demog transaction pen demog transaction.
   *
   * @return the pen demog transaction
   */
  public PenDemogTransaction createMockPenDemogTransaction() {
    return PenDemogTransaction.builder()
      .transactionInsertDateTime(LocalDateTime.now())
      .transactionStatus(TransactionStatus.PENDING.getCode())
      .transactionType(TransactionType.CREATE_STUDENT.getCode())
      .createUser("test")
      .demogCode("A")
      .status("A")
      .sex("M")
      .surname("surname")
      .birthDate("19800101")
      .givenName("givenName")
      .transactionID(this.transactionID)
      .pen(this.pen1)
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
  }
}
