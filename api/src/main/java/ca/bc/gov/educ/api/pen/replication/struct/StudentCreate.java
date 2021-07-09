package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * The type Student create.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
@ToString(callSuper = true)
public class StudentCreate extends BaseStudent implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The History activity code.
   */
  @NotNull(message = "historyActivityCode can not be null.")
  String historyActivityCode;
}
