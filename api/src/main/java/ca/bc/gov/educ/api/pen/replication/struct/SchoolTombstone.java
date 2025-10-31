package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchoolTombstone extends BaseRequest implements Serializable {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private String schoolId;

  private String districtId;

  private String mincode;

  private String independentAuthorityId;

  private String schoolNumber;

  private String faxNumber;

  private String phoneNumber;

  private String email;

  private String website;

  private String displayName;

  private String schoolOrganizationCode;

  private String schoolCategoryCode;

  private String facilityTypeCode;
  
  private String schoolReportingRequirementCode;

  private String vendorSourceSystemCode;

  private String openedDate;

  private String closedDate;

}
