package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * The type School entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AUTHORITY_MAST")
public class AuthorityMasterEntity {
  @Id
  @Column(name = "AUTH_NUMBER", nullable = false, length = 3)
  protected String authNumber;

  @Column(name = "SCHOOL_AUTHORITY_NAME")
  private String name;

  @Column(name = "ADDRESS_LINE_1")
  private String addressLine1;

  @Column(name = "ADDRESS_LINE_2")
  private String addressLine2;

  @Column(name = "CITY")
  private String city;

  @Column(name = "PROVINCE_CODE")
  private String provinceCode;

  @Column(name = "COUNTRY_CODE")
  private String countryCode;

  @Column(name = "POSTAL_CODE")
  private String postalCode;

  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  @Column(name = "FAX_NUMBER")
  private String faxNumber;

  @Column(name = "E_MAIL_ID")
  private String email;

  @Column(name = "TITLE_CODE")
  private String titleCode;

  @Column(name = "GIVEN_NAME")
  private String givenName;

  @Column(name = "MIDDLE_NAME")
  private String middleName;

  @Column(name = "SURNAME")
  private String surname;

  @Column(name = "SCHOOL_UMBRELLA_GROUP_CODE")
  private String schoolUmbrellaGroupCode;

  @Column(name = "INCORPORATION_TYPE_CODE")
  private String incorporationTypeCode;

  @Column(name = "INCORPORATION_NUMBER")
  private String incorporationNumber;

  @Column(name = "AUTH_SUPPLIER_NUMBER")
  private String authSupplierNumber;

  @Column(name = "AUTH_STATUS")
  private String statusCode;

  @Column(name = "AUTH_SOC_STATUS")
  private String socialStatusCode;

  @Column(name = "AUTH_OPEN_DATE")
  private String openedDate;

  @Column(name = "AUTH_CLOSE_DATE")
  private String closedDate;

  @Column(name = "LAST_ANNUAL_FILE_DATE")
  private String lastAnnualFileDate;

  @Column(name = "INCORPORATION_DATE")
  private String incorporationDate;

  @Column(name = "PRIVATE_ACT_NAME")
  private String privateActName;

  @Column(name = "SCHOOL_AUTHORITY_NAME_LONG")
  private String authorityNameLong;

  @Column(name = "VENDOR_LOCATION_CODE")
  private String vendorLocationCode;

  @Column(name = "AUTHORITY_TYPE")
  private String authorityType;

  @Column(name = "DATE_OPENED")
  private LocalDateTime dateOpened;

  @Column(name = "DATE_CLOSED")
  private LocalDateTime dateClosed;
}
