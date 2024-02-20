package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorityUpdateServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private AuthorityCreateService authorityCreateService;
  @Autowired
  private AuthorityUpdateService authorityUpdateService;

  @Before
  public void setUp() throws JsonProcessingException {
    final var request = TestUtils.createIndependentAuthority();
    final var event = TestUtils.createEvent("CREATE_AUTHORITY", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.authorityCreateService.saveAuthority(request);
  }

  @Test
  public void testProcessEvent_givenUPDATE_STUDENT_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    final var request = TestUtils.createIndependentAuthority();
    final var event = TestUtils.createEvent("UPDATE_AUTHORITY", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.authorityUpdateService.processEvent(request, event);
    final var auth = this.penReplicationTestUtils.getAuthorityMasterRepository().findById(request.getAuthorityNumber().trim());
    assertThat(auth).isPresent();
  }

}
