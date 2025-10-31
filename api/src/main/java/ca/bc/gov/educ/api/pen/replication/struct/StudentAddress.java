package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * The type Student.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentAddress extends BaseRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String studentAddressId;
  
  @NotNull(message = "studentID cannot be null")
  private String studentID;

  @Size(max = 255)
  @NotNull(message = "addressLine1 cannot be null")
  private String addressLine1;

  @Size(max = 255)
  private String addressLine2;

  @Size(max = 255)
  @NotNull(message = "city cannot be null")
  private String city;

  @Size(max = 255)
  @NotNull(message = "postalZip cannot be null")
  private String postalZip;

  @Size(max = 10)
  @NotNull(message = "provinceStateCode cannot be null")
  private String provinceStateCode;

  @Size(max = 10)
  @NotNull(message = "countryCode cannot be null")
  private String countryCode;

}
