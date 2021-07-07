package ca.bc.gov.educ.api.pen.replication.controller;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PenReplicationSagaControllerTest extends BasePenReplicationAPITest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testReadSaga_givenInvalidSagaID_shouldReturn404() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-replication/saga/{sagaId}", UUID.randomUUID())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }


  @Test
  public void testGetSagaPaginated_givenNoData_shouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-replication/saga/paginated")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }
}
