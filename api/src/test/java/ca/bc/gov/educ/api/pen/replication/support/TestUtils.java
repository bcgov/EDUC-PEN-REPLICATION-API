package ca.bc.gov.educ.api.pen.replication.support;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;

/**
 * The type Test utils.
 */
public class TestUtils {
  /**
   * Initialize base student request.
   *
   * @param student the student
   */
  public static void initializeBaseStudentRequest(BaseStudent student) {
    student.setStudentID(UUID.randomUUID().toString());
    student.setPen("987654321 ");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob("1907-05-26");
    student.setSexCode("M");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setEmailVerified("Y");
    student.setDeceasedDate("1979-06-11");
    student.setCreateDate("2021-04-23 15:13:45");
    student.setUpdateDate("2021-04-23 15:13:45");
  }

  /**
   * Create student create request student create.
   *
   * @param postalCode the postal code
   * @return the student create
   */
  public static StudentCreate createStudentCreateRequest(String postalCode) {
    StudentCreate student = new StudentCreate();
    TestUtils.initializeBaseStudentRequest(student);
    student.setPostalCode(postalCode);
    student.setHistoryActivityCode("USERNEW");
    return student;
  }

  public static IndependentAuthority createIndependentAuthority() {
    var auth = IndependentAuthority.builder().authorityNumber("003").displayName("IndependentAuthority Name").openedDate(LocalDateTime.now().minusDays(1).toString())
      .authorityTypeCode("INDEPEND").build();
    auth.setCreateDate(LocalDateTime.now().toString());
    auth.setUpdateDate(LocalDateTime.now().toString());
    auth.setCreateUser("ABC");
    auth.setUpdateUser("ABC");
    return auth;
  }

  public static School createSchoolData() {
    var school = School
      .builder()
      .mincode("09812345")
      .schoolNumber("12345")
      .displayName("School Name")
      .openedDate(LocalDateTime.now().minusDays(1).withNano(0).toString())
      .schoolCategoryCode("PUBLIC")
      .schoolOrganizationCode("TWO_SEM")
      .facilityTypeCode("DISTONLINE")
      .website("abc@sd99.edu")
      .build();
    school.setCreateDate(LocalDateTime.now().toString());
    school.setUpdateDate(LocalDateTime.now().toString());
    school.setCreateUser("ABC");
    school.setUpdateUser("ABC");
    return school;
  }

  public static FacilityTypeCode createFacilityTypeCodeData() {
    var fac = FacilityTypeCode.builder().facilityTypeCode("DISTONLINE").description("Standard School").legacyCode("08")
        .effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).displayOrder(1).label("Standard School").build();
    return fac;
  }

  public static SchoolOrganizationCode createSchoolOrganizationCodeData() {
    return SchoolOrganizationCode.builder().schoolOrganizationCode("TWO_SEM").description("Two Semesters").legacyCode("01")
      .effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).displayOrder(1).label("Two Semesters").build();
  }

  public static SchoolCategoryCode createSchoolCategoryCodeData() {
    return SchoolCategoryCode.builder().schoolCategoryCode("PUBLIC").description("Public School").legacyCode("01")
      .effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).displayOrder(1).label("Public School").build();
  }

  public static ProvinceCode createProvinceCodeData() {
    return ProvinceCode.builder().provinceCode("BC").description("British Columbia").legacyCode("BC")
      .effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).displayOrder(1).label("British Columbia").build();
  }

  public static CountryCode createCountryCodeData() {
    return CountryCode.builder().countryCode("CA").description("Canada").legacyCode("CA")
      .effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).displayOrder(1).label("Canada").build();
  }

  /**
   * Create event event.
   *
   * @param eventType       the event type
   * @param payload         the payload
   * @param eventRepository the event repository
   * @return the event
   * @throws JsonProcessingException the json processing exception
   */
  public static Event createEvent(String eventType, Object payload, EventRepository eventRepository) throws JsonProcessingException {
    var event = Event.builder()
      .eventType(eventType)
      .eventId(UUID.randomUUID())
      .eventOutcome("PROCESSED")
      .eventPayload(JsonUtil.getJsonStringFromObject(payload))
      .eventStatus(DB_COMMITTED.toString())
      .createUser("PEN-REPLICATION-API")
      .updateUser("PEN-REPLICATION-API")
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
    eventRepository.save(event);
    return event;
  }
}
