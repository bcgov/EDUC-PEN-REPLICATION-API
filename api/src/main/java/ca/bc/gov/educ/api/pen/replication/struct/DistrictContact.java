package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistrictContact extends BaseRequest implements Serializable {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private String districtContactId;

  private String districtId;

  @Size(max = 10)
  @NotNull(message = "districtContactTypeCode cannot be null")
  private String districtContactTypeCode;

  @Size(max = 10)
  private String phoneNumber;

  private String jobTitle;

  @Size(max = 10)
  private String phoneExtension;

  @Size(max = 10)
  private String alternatePhoneNumber;

  @Size(max = 10)
  private String alternatePhoneExtension;

  @Size(max = 255)
  @Email(message = "Email address should be a valid email address")
  private String email;

  @Size(max = 255)
  private String firstName;

  @Size(max = 255)
  @NotNull(message = "lastName cannot be null")
  private String lastName;

  private String effectiveDate;

  private String expiryDate;
}
