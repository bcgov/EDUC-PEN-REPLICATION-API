package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class SchoolMapperHelper  {

  private static final String MAILING_ADDRESS_TYPE = "MAILING";
  private static final String PHYSICAL_ADDRESS_TYPE = "PHYSICAL";
  private static final String BOOLEAN_YES = "Y";

  private static final String BOOLEAN_NO = "N";

  private RestUtils restUtils;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  public SchoolMapperHelper(final RestUtils restUtils){
    this.restUtils = restUtils;
  }

  public SchoolMasterEntity toSchoolMaster(School s, boolean isCreate) {
    Map<String,FacilityTypeCode> facilityTypeCodeMap = restUtils.getFacilityTypeCodes();
    Map<String,SchoolOrganizationCode> schoolOrganizationCodes = restUtils.getSchoolOrganizationCodes();
    Map<String,SchoolCategoryCode> schoolCategoryCodes = restUtils.getSchoolCategoryCodes();
    Map<String,ProvinceCode> provinceCodes = restUtils.getProvinceCodes();
    Map<String,CountryCode> countryCodes = restUtils.getCountryCodes();
    final var schoolMasterEntity = new SchoolMasterEntity();

    if(isCreate){
      schoolMasterEntity.setCreateDate(Long.valueOf(s.getUpdateDate().substring(0,10).replace("-","")));
      schoolMasterEntity.setCreateTime(Long.valueOf(s.getUpdateDate().substring(11,19).replace(":","")));
      schoolMasterEntity.setCreateUsername(StringUtils.substring(s.getUpdateUser(), 0, 12));
    }

    schoolMasterEntity.setScFaxNumber(StringUtils.substring(s.getFaxNumber(), 0, 10));
    schoolMasterEntity.setScPhoneNumber(StringUtils.substring(s.getPhoneNumber(), 0, 10));
    schoolMasterEntity.setScEMailId(StringUtils.substring(s.getEmail(), 0, 100));

    if(StringUtils.isNotEmpty(s.getDisplayNameNoSpecialChars())) {
      schoolMasterEntity.setSchoolName(StringUtils.substring(s.getDisplayNameNoSpecialChars(), 0, 40));
    }else{
      schoolMasterEntity.setSchoolName(StringUtils.substring(s.getDisplayName(), 0, 40));
    }
    schoolMasterEntity.setFacilityTypeCode(facilityTypeCodeMap.get(s.getFacilityTypeCode()).getLegacyCode());
    schoolMasterEntity.setSchoolOrganizationCode(schoolOrganizationCodes.get(s.getSchoolOrganizationCode()).getLegacyCode());
    schoolMasterEntity.setSchoolCategoryCode(schoolCategoryCodes.get(s.getSchoolCategoryCode()).getLegacyCode());

    switch (s.getFacilityTypeCode()){
      case "DIST_LEARN":
        if(s.getSchoolCategoryCode().equalsIgnoreCase("PUBLIC") || s.getSchoolCategoryCode().equalsIgnoreCase("YUKON")){
          schoolMasterEntity.setOnlineSchoolType("POLSP");
        }else if(s.getSchoolCategoryCode().equalsIgnoreCase("INDEPEND")){
          schoolMasterEntity.setOnlineSchoolType("POLSI");
        }else{
          schoolMasterEntity.setOnlineSchoolType(null);
        }
        break;
      case "DISTONLINE":
        if(s.getSchoolCategoryCode().equalsIgnoreCase("PUBLIC")) {
          schoolMasterEntity.setOnlineSchoolType("DOLS");
        }else{
          schoolMasterEntity.setOnlineSchoolType(null);
        }
        break;
      default:
        schoolMasterEntity.setOnlineSchoolType(null);
        break;
    }

    Mincode mincode = new Mincode();
    mincode.setDistNo(s.getMincode().substring(0,3));
    mincode.setSchlNo(s.getSchoolNumber());
    schoolMasterEntity.setMincode(mincode);
    if(StringUtils.isNotEmpty(s.getIndependentAuthorityId())) {
      schoolMasterEntity.setAuthNumber(restUtils.getIndependentAuthorityByID(s.getIndependentAuthorityId()).getAuthorityNumber());
    }else{
      schoolMasterEntity.setAuthNumber(null);
    }

    if(StringUtils.isNotEmpty(s.getClosedDate()) && dateTimeMapper.map(s.getClosedDate()).isBefore(LocalDateTime.now())){
      schoolMasterEntity.setSchoolStatusCode("C");
    }else{
      schoolMasterEntity.setSchoolStatusCode("O");
    }

    if(StringUtils.isNotEmpty(s.getOpenedDate())) {
      schoolMasterEntity.setOpenedDate(s.getOpenedDate().substring(0, 10).replace("-", ""));
      schoolMasterEntity.setDateOpened(dateTimeMapper.map(s.getOpenedDate()));
    }else{
      schoolMasterEntity.setOpenedDate(null);
      schoolMasterEntity.setDateOpened(null);
    }

    if(StringUtils.isNotEmpty(s.getClosedDate())) {
      schoolMasterEntity.setClosedDate(s.getClosedDate().substring(0, 10).replace("-", ""));
      schoolMasterEntity.setDateClosed(dateTimeMapper.map(s.getClosedDate()));
    }else{
      schoolMasterEntity.setClosedDate(null);
      schoolMasterEntity.setDateClosed(null);
    }

    schoolMasterEntity.setEditDate(Long.valueOf(s.getUpdateDate().substring(0,10).replace("-","")));
    schoolMasterEntity.setEditTime(Long.valueOf(s.getUpdateDate().substring(11,19).replace(":","")));
    schoolMasterEntity.setEditUsername(StringUtils.substring(s.getUpdateUser(), 0, 12));

    schoolMasterEntity.setNlcEarlyLearningFlag(getNLCValueFlag(s,"EARLYLEARN"));
    schoolMasterEntity.setNlcAfterSchoolProgramFlag(getNLCValueFlag(s,"AFTERSCHL"));
    schoolMasterEntity.setNlcContinuingEdFlag(getNLCValueFlag(s,"CONTINEDUC"));
    schoolMasterEntity.setNlcSeniorsFlag(getNLCValueFlag(s,"SENIORS"));
    schoolMasterEntity.setNlcSportAndRecFlag(getNLCValueFlag(s,"SPORTRECR"));
    schoolMasterEntity.setNlcCommunityUseFlag(getNLCValueFlag(s,"COMMUNITY"));
    schoolMasterEntity.setNlcIntegratedServicesFlag(getNLCValueFlag(s,"INTEGRSERV"));

    //Addresses
    var mailAddress = getAddressValueIfExists(s, MAILING_ADDRESS_TYPE);
    if(mailAddress.isPresent()) {
      var addy = mailAddress.get();
      schoolMasterEntity.setScAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,40));
      schoolMasterEntity.setScAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,40));
      schoolMasterEntity.setScCity(StringUtils.substring(addy.getCity(),0,30));
      schoolMasterEntity.setScProvinceCode(provinceCodes.get(addy.getProvinceCode()).getLegacyCode());
      schoolMasterEntity.setScCountryCode(countryCodes.get(addy.getCountryCode()).getLegacyCode());
      schoolMasterEntity.setScPostalCode(StringUtils.upperCase(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6)));
    } else {
      schoolMasterEntity.setScAddressLine1(null);
      schoolMasterEntity.setScAddressLine2(null);
      schoolMasterEntity.setScCity(null);
      schoolMasterEntity.setScProvinceCode(null);
      schoolMasterEntity.setScCountryCode(null);
      schoolMasterEntity.setScPostalCode(null);
    }

    var physAddress = getAddressValueIfExists(s, PHYSICAL_ADDRESS_TYPE);
    if(physAddress.isPresent()) {
      var addy = physAddress.get();
      schoolMasterEntity.setPhysAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,40));
      schoolMasterEntity.setPhysAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,40));
      schoolMasterEntity.setPhysCity(StringUtils.substring(addy.getCity(),0,30));
      schoolMasterEntity.setPhysProvinceCode(provinceCodes.get(addy.getProvinceCode()).getLegacyCode());
      schoolMasterEntity.setPhysCountryCode(countryCodes.get(addy.getCountryCode()).getLegacyCode());
      schoolMasterEntity.setPhysPostalCode(StringUtils.upperCase(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6)));
    } else {
      schoolMasterEntity.setPhysAddressLine1(null);
      schoolMasterEntity.setPhysAddressLine2(null);
      schoolMasterEntity.setPhysCity(null);
      schoolMasterEntity.setPhysProvinceCode(null);
      schoolMasterEntity.setPhysCountryCode(null);
      schoolMasterEntity.setPhysPostalCode(null);
    }

    //Principal
    schoolMasterEntity.setPrGivenName(null);
    schoolMasterEntity.setPrSurname(null);
    schoolMasterEntity.setPrMiddleName(null);
    schoolMasterEntity.setPrTitleCode(null);

    return schoolMasterEntity;
  }

  private Optional<SchoolAddress> getAddressValueIfExists(School school, String addressTypeCode){
    if(school.getAddresses() != null){
      var addresses = school.getAddresses().stream().filter(schoolAddress -> schoolAddress.getAddressTypeCode().equals(addressTypeCode)).toList();
      if(!addresses.isEmpty()){
        return Optional.of(addresses.get(0));
      }
    }
    return Optional.empty();
  }

  private String getNLCValueFlag(School school, String nlcTypeCode){
    if(school.getNeighborhoodLearning() != null){
      var nlcs = school.getNeighborhoodLearning().stream().filter(neighborhoodLearning -> neighborhoodLearning.getNeighborhoodLearningTypeCode().equals(nlcTypeCode)).toList();
      if(!nlcs.isEmpty()){
        return BOOLEAN_YES;
      }
    }
    return BOOLEAN_NO;
  }
}
