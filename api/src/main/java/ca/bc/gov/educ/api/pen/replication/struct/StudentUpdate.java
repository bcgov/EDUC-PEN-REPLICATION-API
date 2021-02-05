package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * The type Student update.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
public class StudentUpdate extends BaseStudent implements Serializable {
  private static final long serialVersionUID = 1L;
  /**
   * The History activity code.
   */
  @NotNull(message = "historyActivityCode can not be null.")
  String historyActivityCode;
}
