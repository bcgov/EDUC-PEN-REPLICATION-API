package ca.bc.gov.educ.api.pen.replication.struct;

import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
public class PossibleMatch extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 4270771037046576398L;
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
