package ca.bc.gov.educ.api.pen.replication.controller;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Pen replication saga controller test.
 */
public class PenReplicationSagaControllerTest extends BasePenReplicationAPITest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  SagaService sagaService;

  /**
   * Test read saga given invalid saga id should return 404.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReadSaga_givenInvalidSagaID_shouldReturn404() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-replication/saga/{sagaId}", UUID.randomUUID())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testUpdateSaga_givenNoBody_shouldReturn400() throws Exception {
    this.mockMvc.perform(put("/api/v1/pen-replication/saga/{sagaId}", UUID.randomUUID())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateSaga_givenInvalidID_shouldReturn404() throws Exception {
    val saga = createMockSaga();
    this.mockMvc.perform(put("/api/v1/pen-replication/saga/{sagaId}", UUID.randomUUID()).content(JsonUtil.objectMapper.writeValueAsBytes(saga))
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  private Saga createMockSaga() {
    return Saga.builder().sagaId(UUID.randomUUID()).payload("test").build();
  }


  /**
   * Test get saga paginated given no data should return status ok.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSagaPaginated_givenNoData_shouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-replication/saga/paginated")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testReadSagaEvents_givenSagaDoesntExist_shouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-replication/saga//{sagaID}/events", UUID.randomUUID())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testGetSagaBySagaID_whenSagaIDIsValidWithNoEvents_shouldReturnStatusOk() throws Exception {
    var saga = this.sagaService.createSagaRecordInDB(SagaEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA.getCode(), "JOCOX", "test");
    this.repository.save(saga);
    this.mockMvc.perform(get("/api/v1/pen-replication/saga//{sagaID}/events", saga.getSagaId()).with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_REPLICATION_READ_SAGA")))).andDo(print()).andExpect(status().isOk());
  }

}
