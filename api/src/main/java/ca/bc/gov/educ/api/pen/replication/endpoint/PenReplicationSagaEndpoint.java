package ca.bc.gov.educ.api.pen.replication.endpoint;

import ca.bc.gov.educ.api.pen.replication.struct.saga.Saga;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SagaEvent;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * The interface Pen replication saga endpoint.
 */
@RequestMapping("/api/v1/pen-replication/saga")
public interface PenReplicationSagaEndpoint {

  /**
   * Read saga response entity.
   *
   * @param sagaID the saga id
   * @return the response entity
   */
  @GetMapping("/{sagaID}")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve saga by its ID (GUID).", description = "Endpoint to retrieve saga by its ID (GUID).")
  ResponseEntity<Saga> readSaga(@PathVariable UUID sagaID);


  @PutMapping("/{sagaID}")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_WRITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to update saga by its ID (GUID).", description = "Endpoint to update saga by its ID (GUID).")
  ResponseEntity<Saga> updateSaga(@PathVariable UUID sagaID, @RequestBody Saga saga);

  @PutMapping("/{sagaID}/saga-event-states/{sagaEventID}")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_WRITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to update saga event by its ID (GUID) and Saga ID (GUID).", description = "Endpoint to update saga by its ID (GUID) and Saga ID (GUID).")
  ResponseEntity<SagaEvent> updateSagaEventState(@PathVariable UUID sagaID, @PathVariable UUID sagaEventID, @RequestBody SagaEvent sagaEvent);

  @GetMapping("/{sagaID}/events")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
  ResponseEntity<List<SagaEvent>> getSagaEventsBySagaID(@PathVariable UUID sagaID);

  @DeleteMapping("/{sagaID}/saga-event-states/{sagaEventID}")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_WRITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to delete saga event by its ID (GUID) and Saga ID (GUID).", description = "Endpoint to delete saga by its ID (GUID) and Saga ID (GUID).")
  ResponseEntity<Void> deleteSagaEvent(@PathVariable UUID sagaID, @PathVariable UUID sagaEventID);

  /**
   * Find all Sagas for given search criteria.
   *
   * @param pageNumber             the page number
   * @param pageSize               the page size
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search list , the JSON string ( of Array or List of {@link ca.bc.gov.educ.api.pen.replication.struct.Search})
   * @return Page {@link Saga}
   */
  @GetMapping("/paginated")
  @PreAuthorize("hasAuthority('SCOPE_PEN_REPLICATION_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support data table view in frontend, with sort, filter and pagination, for Sagas.", description = "This API endpoint exposes flexible way to query the entity by leveraging JPA specifications.")
  ResponseEntity<Page<Saga>> findAllSagas(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                          @ArraySchema(schema = @Schema(name = "searchCriteriaList",
                                            description = "searchCriteriaList if provided should be a JSON string of Search Array",
                                            implementation = ca.bc.gov.educ.api.pen.replication.struct.Search.class))
                                          @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);
}
