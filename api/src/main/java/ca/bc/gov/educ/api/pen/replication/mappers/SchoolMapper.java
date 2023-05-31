package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring", uses = {UUIDMapper.class, LocalDateTimeMapper.class })
public interface SchoolMapper {

  SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

  @Mapping(target = "schoolTypeCode", ignore = true)
  @Mapping(target = "numberOfDivisions", ignore = true)
  @Mapping(target = "numberOfSecFteTeachers", ignore = true)
  @Mapping(target = "numberOfElmFteTeachers", ignore = true)
  @Mapping(target = "ttblElemInstrMinutes", ignore = true)
  @Mapping(target = "enrolHeadcount1523", ignore = true)
  @Mapping(target = "enrolHeadcount1701", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "createUsername", ignore = true)
  @Mapping(target = "elemTeachersHc", ignore = true)
  @Mapping(target = "secTeachersHc", ignore = true)
  @Mapping(target = "contedFundFlag", ignore = true)
  @Mapping(target = "elemFteClassroom", ignore = true)
  @Mapping(target = "elemFteSupport", ignore = true)
  @Mapping(target = "elemFteAdmin", ignore = true)
  @Mapping(target = "secFteClassroom", ignore = true)
  @Mapping(target = "secFteSupport", ignore = true)
  @Mapping(target = "secFteAdmin", ignore = true)
  @Mapping(target = "educMethodClassCnt", ignore = true)
  @Mapping(target = "educMethodDelCnt", ignore = true)
  @Mapping(target = "educMethodBothCnt", ignore = true)
  @Mapping(target = "newDistno", ignore = true)
  @Mapping(target = "newSchlno", ignore = true)
  @Mapping(target = "assetNumber", ignore = true)
  @Mapping(target = "assetAssignedBy", ignore = true)
  @Mapping(target = "assetAssignedDate", ignore = true)
  @Mapping(target = "assetChangedBy", ignore = true)
  @Mapping(target = "assetChangedDate", ignore = true)
  @Mapping(target = "restrictFunding", ignore = true)
  void updateSchoolMaster(SchoolMasterEntity schoolMasterEntity, @MappingTarget SchoolMasterEntity targetEntity);
}
