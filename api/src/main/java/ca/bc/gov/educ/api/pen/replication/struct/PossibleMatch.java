package ca.bc.gov.educ.api.pen.replication.struct;

import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * The type Possible match.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PossibleMatch extends BaseRequest {
  /**
   * The Possible match id.
   */
  String possibleMatchID;
  /**
   * The Student id.
   */
  @NotNull
  String studentID;
  /**
   * The Matched student id.
   */
  @NotNull
  String matchedStudentID;
  /**
   * The Match reason code.
   */
  @NotNull
  MatchReasonCodes matchReasonCode;
}
