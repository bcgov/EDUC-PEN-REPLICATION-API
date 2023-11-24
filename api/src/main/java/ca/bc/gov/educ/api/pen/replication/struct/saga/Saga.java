package ca.bc.gov.educ.api.pen.replication.struct.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * The type Saga.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Saga {
  /**
   * The Saga id.
   */
  UUID sagaId;
  /**
   * Created by event id.
   */
  UUID createdFromEventID;
  /**
   * The Saga name.
   */
  String sagaName;
  /**
   * The Saga state.
   */
  String sagaState;
  /**
   * The Payload.
   */
  String payload;
  /**
   * The Status.
   */
  String status;
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
  /**
   * The Create date.
   */
  String createDate;
  /**
   * The Update date.
   */
  String updateDate;

  Integer retryCount;
}
