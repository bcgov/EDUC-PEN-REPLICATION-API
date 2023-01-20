package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Student decorator.
 */
public abstract class SchoolDecorator implements SchoolMapper {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  protected RestUtils restUtils;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  protected SchoolMapper delegate;

  private static final String MAILING_ADDRESS_TYPE = "MAILING";
  private static final String PHYSICAL_ADDRESS_TYPE = "PHYSICAL";
  private static final String PRINCIPAL_TYPE = "PRINCIPAL";
  private static final String BOOLEAN_YES = "Y";

  private static final String BOOLEAN_NO = "N";

  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();


  @Override
  public SchoolMasterEntity toSchoolMaster(School s) {
    Map<String,FacilityTypeCode> facilityTypeCodeMap = restUtils.getFacilityTypeCodes();
    Map<String,SchoolOrganizationCode> schoolOrganizationCodes = restUtils.getSchoolOrganizationCodes();
    Map<String,SchoolCategoryCode> schoolCategoryCodes = restUtils.getSchoolCategoryCodes();
    final var schoolMasterEntity = this.delegate.toSchoolMaster(s);

    schoolMasterEntity.setScFaxNumber(StringUtils.substring(s.getFaxNumber(), 0, 10));
    schoolMasterEntity.setScPhoneNumber(StringUtils.substring(s.getPhoneNumber(), 0, 10));
    schoolMasterEntity.setScEMailId(StringUtils.substring(s.getEmail(), 0, 100));

    schoolMasterEntity.setSchoolName(StringUtils.substring(s.getDisplayName(), 0, 40));
    schoolMasterEntity.setFacilityTypeCode(facilityTypeCodeMap.get(s.getFacilityTypeCode()).getLegacyCode());
    schoolMasterEntity.setSchoolOrganizationCode(schoolOrganizationCodes.get(s.getSchoolOrganizationCode()).getLegacyCode());
    schoolMasterEntity.setSchoolCategoryCode(schoolCategoryCodes.get(s.getSchoolCategoryCode()).getLegacyCode());

    schoolMasterEntity.getMincode().setDistNo(s.getMincode().substring(0,3));
    schoolMasterEntity.getMincode().setSchlNo(s.getSchoolNumber());
    if(StringUtils.isNotEmpty(s.getIndependentAuthorityId())) {
      schoolMasterEntity.setAuthNumber(restUtils.getIndependentAuthorityByID(s.getIndependentAuthorityId()).getAuthorityNumber());
    }

    if(StringUtils.isNotEmpty(s.getClosedDate()) && dateTimeMapper.map(s.getClosedDate()).isBefore(LocalDateTime.now())){
      schoolMasterEntity.setSchoolStatusCode("C");
    }else{
      schoolMasterEntity.setSchoolStatusCode("O");
    }

    if(StringUtils.isNotEmpty(s.getOpenedDate())) {
      schoolMasterEntity.setOpenedDate(s.getOpenedDate().substring(0, 10).replace("-", ""));
      schoolMasterEntity.setDateOpened(dateTimeMapper.map(s.getOpenedDate()));
    }
    if(StringUtils.isNotEmpty(s.getClosedDate())) {
      schoolMasterEntity.setClosedDate(s.getClosedDate().substring(0, 10).replace("-", ""));
      schoolMasterEntity.setDateClosed(dateTimeMapper.map(s.getClosedDate()));
    }

    schoolMasterEntity.setEditDate(Long.valueOf(s.getUpdateDate().substring(0,10).replace("-","")));
    schoolMasterEntity.setEditTime(Long.valueOf(s.getUpdateDate().substring(10,19).replace(":","")));
    schoolMasterEntity.setEditUsername(StringUtils.substring(s.getUpdateUser(), 0, 12));

    // These can be ignored
//    schoolMasterEntity.setSchoolTypeCode();
//    schoolMasterEntity.setAssetAssignedBy();
//    schoolMasterEntity.setNewDistno();
//    schoolMasterEntity.setNewSchlno();
//    schoolMasterEntity.setAssetNumber();
//    schoolMasterEntity.setAssetAssignedBy();
//    schoolMasterEntity.setAssetAssignedDate();
//    schoolMasterEntity.setAssetChangedBy();
//    schoolMasterEntity.setAssetChangedDate();
//    schoolMasterEntity.setGrade79Ind(getGradeValueFlag(s, "GRADE01"));
//    schoolMasterEntity.setGrade89Ind(getGradeValueFlag(s, "GRADE01"));
//    schoolMasterEntity.setGrade29Ind(getGradeValueFlag(s, "GRADE01"));
//    schoolMasterEntity.setCreateTime();
//    schoolMasterEntity.setCreateUsername();
//    schoolMasterEntity.setCreateDate();
//    schoolMasterEntity.setNumberOfDivisions();
//    schoolMasterEntity.setNumberOfSecFteTeachers();
//    schoolMasterEntity.setNumberOfElmFteTeachers();
//    schoolMasterEntity.setTtblElemInstrMinutes();
//    schoolMasterEntity.setEnrolHeadcount1523();
//    schoolMasterEntity.setEnrolHeadcount1701();
//    schoolMasterEntity.setElemFteClassroom();
//    schoolMasterEntity.setElemFteSupport();
//    schoolMasterEntity.setElemFteAdmin();
//    schoolMasterEntity.setSecFteClassroom();
//    schoolMasterEntity.setSecFteSupport();
//    schoolMasterEntity.setSecFteAdmin();
//    schoolMasterEntity.setEducMethodClassCnt();
//    schoolMasterEntity.setEducMethodDelCnt();
//    schoolMasterEntity.setEducMethodBothCnt();
//    schoolMasterEntity.setElemTeachersHc();
//    schoolMasterEntity.setSecTeachersHc();
//    schoolMasterEntity.setRestrictFunding();
//    schoolMasterEntity.setContedFundFlag();


    schoolMasterEntity.setGrade01Ind(getGradeValueFlag(s, "GRADE01"));
    schoolMasterEntity.setGrade02Ind(getGradeValueFlag(s, "GRADE02"));
    schoolMasterEntity.setGrade03Ind(getGradeValueFlag(s, "GRADE03"));
    schoolMasterEntity.setGrade04Ind(getGradeValueFlag(s, "GRADE04"));
    schoolMasterEntity.setGrade05Ind(getGradeValueFlag(s, "GRADE05"));
    schoolMasterEntity.setGrade06Ind(getGradeValueFlag(s, "GRADE06"));
    schoolMasterEntity.setGrade07Ind(getGradeValueFlag(s, "GRADE07"));
    schoolMasterEntity.setGrade08Ind(getGradeValueFlag(s, "GRADE08"));
    schoolMasterEntity.setGrade09Ind(getGradeValueFlag(s, "GRADE09"));
    schoolMasterEntity.setGrade10Ind(getGradeValueFlag(s, "GRADE10"));
    schoolMasterEntity.setGrade11Ind(getGradeValueFlag(s, "GRADE11"));
    schoolMasterEntity.setGrade12Ind(getGradeValueFlag(s, "GRADE12"));

    schoolMasterEntity.setGradeKhInd(getGradeValueFlag(s, "KINDHALF"));
    schoolMasterEntity.setGradeKfInd(getGradeValueFlag(s, "KINDFULL"));
    schoolMasterEntity.setGradeEuInd(getGradeValueFlag(s, "ELEMUNGR"));
    schoolMasterEntity.setGradeSuInd(getGradeValueFlag(s, "SECUNGR"));
    schoolMasterEntity.setGradeHsInd(getGradeValueFlag(s, "HOMESCHL"));
    schoolMasterEntity.setGradeGaInd(getGradeValueFlag(s, "GRADADULT"));

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
      schoolMasterEntity.setScProvinceCode(addy.getProvinceCode());
      schoolMasterEntity.setScCountryCode(addy.getCountryCode());
      schoolMasterEntity.setScPostalCode(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6));
    }

    var physAddress = getAddressValueIfExists(s, PHYSICAL_ADDRESS_TYPE);
    if(physAddress.isPresent()) {
      var addy = physAddress.get();
      schoolMasterEntity.setPhysAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,40));
      schoolMasterEntity.setPhysAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,40));
      schoolMasterEntity.setPhysCity(StringUtils.substring(addy.getCity(),0,30));
      schoolMasterEntity.setPhysProvinceCode(addy.getProvinceCode());
      schoolMasterEntity.setPhysCountryCode(addy.getCountryCode());
      schoolMasterEntity.setPhysPostalCode(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6));
    }

    //Principal
    var principal = getPrincipalIfExists(s);
    if(principal.isPresent()) {
      var prince = principal.get();
      schoolMasterEntity.setPrGivenName(StringUtils.substring(prince.getFirstName(),0,25));
      schoolMasterEntity.setPrSurname(StringUtils.substring(prince.getLastName(),0,25));
      schoolMasterEntity.setPrMiddleName(null);
      schoolMasterEntity.setPrTitleCode(null);
    }

    return schoolMasterEntity;
  }

  private Optional<SchoolContact> getPrincipalIfExists(School school){
    if(school.getContacts() != null){
      var principals = school.getContacts().stream().filter(schoolContact -> schoolContact.getSchoolContactTypeCode().equals(PRINCIPAL_TYPE)).collect(Collectors.toList());
      if(!principals.isEmpty()){
        return Optional.of(principals.get(0));
      }
    }
    return Optional.empty();
  }

  private Optional<SchoolAddress> getAddressValueIfExists(School school, String addressTypeCode){
    if(school.getAddresses() != null){
      var addresses = school.getAddresses().stream().filter(schoolAddress -> schoolAddress.getAddressTypeCode().equals(addressTypeCode)).collect(Collectors.toList());
      if(!addresses.isEmpty()){
        return Optional.of(addresses.get(0));
      }
    }
    return Optional.empty();
  }

  private String getNLCValueFlag(School school, String nlcTypeCode){
    if(school.getNeighborhoodLearning() != null){
      var nlcs = school.getNeighborhoodLearning().stream().filter(neighborhoodLearning -> neighborhoodLearning.getNeighborhoodLearningTypeCode().equals(nlcTypeCode)).collect(Collectors.toList());
      if(!nlcs.isEmpty()){
        return BOOLEAN_YES;
      }
    }
    return BOOLEAN_NO;
  }

  private String getGradeValueFlag(School school, String gradeTypeCode){
    if(school.getGrades() != null){
      var grades = school.getGrades().stream().filter(schoolGrade -> schoolGrade.getSchoolGradeCode().equals(gradeTypeCode)).collect(Collectors.toList());
      if(!grades.isEmpty()){
        return BOOLEAN_YES;
      }
    }
    return BOOLEAN_NO;
  }

}
