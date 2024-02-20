package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SchoolUpdateServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private SchoolCreateService schoolCreateService;
  @Autowired
  private SchoolUpdateService schoolUpdateService;
  @Autowired
  RestUtils restUtils;

  @Before
  public void setUp() throws JsonProcessingException {
    Map<String, FacilityTypeCode> facilityTypes = new HashMap<>();
    facilityTypes.put("DISTONLINE", TestUtils.createFacilityTypeCodeData());
    when(this.restUtils.getFacilityTypeCodes()).thenReturn(facilityTypes);
    Map<String, SchoolOrganizationCode> organizationCodes = new HashMap<>();
    organizationCodes.put("TWO_SEM", TestUtils.createSchoolOrganizationCodeData());
    when(this.restUtils.getSchoolOrganizationCodes()).thenReturn(organizationCodes);
    Map<String, SchoolCategoryCode> categoryCodes = new HashMap<>();
    categoryCodes.put("PUBLIC", TestUtils.createSchoolCategoryCodeData());
    when(this.restUtils.getSchoolCategoryCodes()).thenReturn(categoryCodes);
    Map<String, ProvinceCode> provinceCodes = new HashMap<>();
    provinceCodes.put("BC", TestUtils.createProvinceCodeData());
    when(this.restUtils.getProvinceCodes()).thenReturn(provinceCodes);
    Map<String, CountryCode> countryCodes = new HashMap<>();
    countryCodes.put("CA", TestUtils.createCountryCodeData());
    when(this.restUtils.getCountryCodes()).thenReturn(countryCodes);
    final var request = TestUtils.createSchoolData();
    final var event = TestUtils.createEvent("CREATE_SCHOOL", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.schoolCreateService.saveSchool(request);
  }

  @Test
  public void testProcessEvent_givenUPDATE_SCHOOL_EventWithNullPostalCode_shouldSaveBlankPostalCodeInDB() throws JsonProcessingException {
    final var request = TestUtils.createSchoolData();
    final var event = TestUtils.createEvent("UPDATE_SCHOOL", request, this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    this.schoolUpdateService.processEvent(request, event);
    Mincode mincode = new Mincode();
    mincode.setDistNo("098");
    mincode.setSchlNo("12345");
    final var schoolMaster = this.penReplicationTestUtils.getSchoolMasterRepository().findById(mincode);
    assertThat(schoolMaster).isPresent();
  }

}
