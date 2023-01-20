package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring", uses = {UUIDMapper.class, LocalDateTimeMapper.class })
@DecoratedWith(SchoolDecorator.class)
public interface SchoolMapper {

  SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

  @Mapping(target = "mincode", ignore = true)
  @Mapping(target = "scAddressLine1", ignore = true)
  @Mapping(target = "scAddressLine2", ignore = true)
  @Mapping(target = "scCity", ignore = true)
  @Mapping(target = "scProvinceCode", ignore = true)
  @Mapping(target = "scCountryCode", ignore = true)
  @Mapping(target = "scPostalCode", ignore = true)
  @Mapping(target = "scFaxNumber", ignore = true)
  @Mapping(target = "scPhoneNumber", ignore = true)
  @Mapping(target = "scEMailId", ignore = true)
  @Mapping(target = "facilityTypeCode", ignore = true)
  @Mapping(target = "schoolName", ignore = true)
  @Mapping(target = "schoolTypeCode", ignore = true)
  @Mapping(target = "schoolOrganizationCode", ignore = true)
  @Mapping(target = "schoolCategoryCode", ignore = true)
  @Mapping(target = "prGivenName", ignore = true)
  @Mapping(target = "prSurname", ignore = true)
  @Mapping(target = "prMiddleName", ignore = true)
  @Mapping(target = "prTitleCode", ignore = true)
  @Mapping(target = "numberOfDivisions", ignore = true)
  @Mapping(target = "numberOfSecFteTeachers", ignore = true)
  @Mapping(target = "numberOfElmFteTeachers", ignore = true)
  @Mapping(target = "ttblElemInstrMinutes", ignore = true)
  @Mapping(target = "schoolStatusCode", ignore = true)
  @Mapping(target = "enrolHeadcount1523", ignore = true)
  @Mapping(target = "enrolHeadcount1701", ignore = true)
  @Mapping(target = "grade01Ind", ignore = true)
  @Mapping(target = "grade29Ind", ignore = true)
  @Mapping(target = "grade04Ind", ignore = true)
  @Mapping(target = "grade05Ind", ignore = true)
  @Mapping(target = "grade06Ind", ignore = true)
  @Mapping(target = "grade07Ind", ignore = true)
  @Mapping(target = "grade08Ind", ignore = true)
  @Mapping(target = "grade09Ind", ignore = true)
  @Mapping(target = "grade10Ind", ignore = true)
  @Mapping(target = "grade11Ind", ignore = true)
  @Mapping(target = "grade12Ind", ignore = true)
  @Mapping(target = "grade79Ind", ignore = true)
  @Mapping(target = "grade89Ind", ignore = true)
  @Mapping(target = "openedDate", ignore = true)
  @Mapping(target = "closedDate", ignore = true)
  @Mapping(target = "authNumber", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "createUsername", ignore = true)
  @Mapping(target = "editDate", ignore = true)
  @Mapping(target = "editTime", ignore = true)
  @Mapping(target = "editUsername", ignore = true)
  @Mapping(target = "elemTeachersHc", ignore = true)
  @Mapping(target = "secTeachersHc", ignore = true)
  @Mapping(target = "gradeKhInd", ignore = true)
  @Mapping(target = "gradeKfInd", ignore = true)
  @Mapping(target = "grade02Ind", ignore = true)
  @Mapping(target = "grade03Ind", ignore = true)
  @Mapping(target = "gradeEuInd", ignore = true)
  @Mapping(target = "gradeSuInd", ignore = true)
  @Mapping(target = "gradeHsInd", ignore = true)
  @Mapping(target = "contedFundFlag", ignore = true)
  @Mapping(target = "elemFteClassroom", ignore = true)
  @Mapping(target = "elemFteSupport", ignore = true)
  @Mapping(target = "elemFteAdmin", ignore = true)
  @Mapping(target = "secFteClassroom", ignore = true)
  @Mapping(target = "secFteSupport", ignore = true)
  @Mapping(target = "secFteAdmin", ignore = true)
  @Mapping(target = "physAddressLine1", ignore = true)
  @Mapping(target = "physAddressLine2", ignore = true)
  @Mapping(target = "physCity", ignore = true)
  @Mapping(target = "physProvinceCode", ignore = true)
  @Mapping(target = "physCountryCode", ignore = true)
  @Mapping(target = "physPostalCode", ignore = true)
  @Mapping(target = "educMethodClassCnt", ignore = true)
  @Mapping(target = "educMethodDelCnt", ignore = true)
  @Mapping(target = "educMethodBothCnt", ignore = true)
  @Mapping(target = "newDistno", ignore = true)
  @Mapping(target = "newSchlno", ignore = true)
  @Mapping(target = "dateOpened", ignore = true)
  @Mapping(target = "dateClosed", ignore = true)
  @Mapping(target = "assetNumber", ignore = true)
  @Mapping(target = "assetAssignedBy", ignore = true)
  @Mapping(target = "assetAssignedDate", ignore = true)
  @Mapping(target = "assetChangedBy", ignore = true)
  @Mapping(target = "assetChangedDate", ignore = true)
  @Mapping(target = "restrictFunding", ignore = true)
  @Mapping(target = "gradeGaInd", ignore = true)
  @Mapping(target = "nlcEarlyLearningFlag", ignore = true)
  @Mapping(target = "nlcAfterSchoolProgramFlag", ignore = true)
  @Mapping(target = "nlcContinuingEdFlag", ignore = true)
  @Mapping(target = "nlcSeniorsFlag", ignore = true)
  @Mapping(target = "nlcSportAndRecFlag", ignore = true)
  @Mapping(target = "nlcCommunityUseFlag", ignore = true)
  @Mapping(target = "nlcIntegratedServicesFlag", ignore = true)
  SchoolMasterEntity toSchoolMaster(School school);

  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "createUsername", ignore = true)
  void updateSchoolMaster(SchoolMasterEntity schoolMasterEntity, @MappingTarget SchoolMasterEntity targetEntity);
}
