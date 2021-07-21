package ca.bc.gov.educ.api.pen.replication.struct.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * The type Saga event.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SagaEvent implements Serializable {

  private static final long serialVersionUID = -3204925065047239143L;
  /**
   * The Saga event id.
   */
  UUID sagaEventId;


  UUID sagaId;

  /**
   * The Saga event state.
   */
  String sagaEventState;

  /**
   * The Saga event outcome.
   */
  String sagaEventOutcome;

  /**
   * The Saga step number.
   */
  Integer sagaStepNumber;

  /**
   * The Saga event response.
   */
  String sagaEventResponse;

}
