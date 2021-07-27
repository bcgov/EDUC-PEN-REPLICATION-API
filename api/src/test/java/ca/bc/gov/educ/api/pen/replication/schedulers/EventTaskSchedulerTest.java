package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTaskSchedulerTest extends BasePenReplicationAPITest {

  @Autowired
  EventTaskScheduler eventTaskScheduler;

  @Before
  public void before() {
    LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @Test
  public void findAndProcessUncompletedSagas() {
    Saga saga = Saga.builder().sagaName(SagaEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA.getCode()).sagaState(EventType.INITIATED.toString()).createUser("TEST").updateUser("TEST").status("STARTED").createDate(LocalDateTime.now().minusHours(2)).updateDate(LocalDateTime.now().minusHours(1)).payload(createMockStudentPayload()).retryCount(2).build();
    penReplicationTestUtils.getSagaRepository().save(saga);
    val sagaId = saga.getSagaId();
    eventTaskScheduler.findAndProcessUncompletedSagas();
    val sagaFromDB = this.penReplicationTestUtils.getSagaRepository().findById(sagaId);
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getRetryCount()).isNotNull().isPositive().isEqualTo(3);
  }

  private String createMockStudentPayload() {
    return "{\n" +
      "  \"penDemogTransaction\": {\n" +
      "    \"transactionID\": \"1234567980\",\n" +
      "    \"transactionType\": \"CS\",\n" +
      "    \"transactionStatus\": \"PEND\",\n" +
      "    \"transactionInsertDateTime\": \"2019-10-19 22:59:05\",\n" +
      "    \"pen\": \"2348845626\",\n" +
      "    \"surname\": \"fake_data\",\n" +
      "    \"givenName\": \"fake_data\",\n" +
      "    \"middleName\": \"fake_data\",\n" +
      "    \"usualSurname\": \"fake_data\",\n" +
      "    \"usualGivenName\": \"fake_data\",\n" +
      "    \"usualMiddleName\": \"fake_data\",\n" +
      "    \"birthDate\": \"19800101\",\n" +
      "    \"sex\": \"M\",\n" +
      "    \"demogCode\": \"A\",\n" +
      "    \"status\": \"A\",\n" +
      "    \"penLocalID\": \"123456789\",\n" +
      "    \"penMinCode\": \"10200001\",\n" +
      "    \"postal\": \"V8T0B1\",\n" +
      "    \"grade\": \"12\",\n" +
      "    \"createUser\": \"fake_data\",\n" +
      "    \"createDate\": \"2028-07-13 18:41:36\",\n" +
      "    \"updateUser\": \"fake_data\",\n" +
      "    \"updateDate\": \"2017-09-02 03:54:28\",\n" +
      "    \"mergeToUserName\": \"fake_data\",\n" +
      "    \"mergeToCode\": \"fake_data\",\n" +
      "    \"mergeToDate\": \"2020-08-20 17:51:36\",\n" +
      "    \"updateDemogDate\": \"2029-01-11 20:26:07\"\n" +
      "  }\n" +
      "}";
  }
}
