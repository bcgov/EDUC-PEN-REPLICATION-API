package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


public class AuthorityCreateServiceTest extends BasePenReplicationAPITest {


  @Autowired
  private AuthorityCreateService authorityCreateService;

  @Test
  public void testProcessEvent_givenCREATE_AUTHORITY_Event_shouldSaveInDB() throws JsonProcessingException {
    final var request = TestUtils.createIndependentAuthority();
    final var event = TestUtils.createEvent("CREATE_AUTHORITY", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.authorityCreateService.processEvent(request, event);
    final var authorityMaster = this.penReplicationTestUtils.getAuthorityMasterRepository().findById(request.getAuthorityNumber().trim());
    assertThat(authorityMaster).isPresent();
  }

}
