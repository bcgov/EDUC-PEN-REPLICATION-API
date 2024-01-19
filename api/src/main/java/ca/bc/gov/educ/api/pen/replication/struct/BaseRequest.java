package ca.bc.gov.educ.api.pen.replication.struct;

import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Base request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseRequest implements Serializable {
  private static final long serialVersionUID = 3132570181228559697L;
  /**
   * The Create user.
   */
  @Size(max = 100)
  public String createUser;
  /**
   * The Update user.
   */
  @Size(max = 100)
  public String updateUser;
  /**
   * The Create date.
   */
  @Null(message = "createDate should be null.")
  public String createDate;
  /**
   * The Update date.
   */
  @Null(message = "updateDate should be null.")
  public String updateDate;
}
