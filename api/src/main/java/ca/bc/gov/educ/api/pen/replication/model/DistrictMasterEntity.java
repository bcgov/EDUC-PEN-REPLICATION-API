package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * The type School entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DISTRICT_MAST")
public class DistrictMasterEntity {
  @Id
  @Column(name = "DISTNO", nullable = false, length = 3)
  protected String distNo;

  @Column(name = "E_MAIL_ID")
  private String email;

  @Column(name = "DISTRICT_NAME")
  private String name;

  @Column(name = "SHRT_DISTRICT_NAME")
  private String shortName;

  @Column(name = "DISTRICT_STATUS_CODE")
  private String districtStatusCode;

  @Column(name = "DIST_AREA_CODE")
  private String districtAreaCode;

  @Column(name = "PHONE_AREA")
  private String phoneAreaCode;

  @Column(name = "PHONE_NUMBER")
  private String phoneNumberNoArea;

  @Column(name = "FAX_AREA")
  private String faxAreaCode;

  @Column(name = "FAX_NUMBER")
  private String faxNumberNoArea;

  @Column(name = "MAIL_ADDRESS_LINE_1")
  private String mailAddressLine1;

  @Column(name = "MAIL_ADDRESS_LINE_2")
  private String mailAddressLine2;

  @Column(name = "MAIL_CITY")
  private String mailCity;

  @Column(name = "MAIL_PROVINCE_CODE")
  private String mailProvinceCode;

  @Column(name = "MAIL_COUNTRY_CODE")
  private String mailCountryCode;

  @Column(name = "MAIL_POSTAL_CODE")
  private String mailPostalCode;

  @Column(name = "MAIL_ADDR_VALID_STATUS")
  private String mailAddrValidStatus;

  @Column(name = "MAIL_ADDR_VALID_DATE")
  private LocalDate mailAddrValidDate;

  @Column(name = "PHYS_ADDRESS_LINE_1")
  private String physAddressLine1;

  @Column(name = "PHYS_ADDRESS_LINE_2")
  private String physAddressLine2;

  @Column(name = "PHYS_CITY")
  private String physCity;

  @Column(name = "PHYS_PROVINCE_CODE")
  private String physProvinceCode;

  @Column(name = "PHYS_COUNTRY_CODE")
  private String physCountryCode;

  @Column(name = "PHYS_POSTAL_CODE")
  private String physPostalCode;

  @Column(name = "PHYS_ADDR_VALID_STATUS")
  private String physAddrValidStatus;

  @Column(name = "PHYS_ADDR_VALID_DATE")
  private LocalDate physAddrValidDate;

  @Column(name = "WEB_ADDRESS")
  private String webAddress;

  @Column(name = "MLAS")
  private String mlas;

  @Column(name = "NOTES")
  private String notes;

}
