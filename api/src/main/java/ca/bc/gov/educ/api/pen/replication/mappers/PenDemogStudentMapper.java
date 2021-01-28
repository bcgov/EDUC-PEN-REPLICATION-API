package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.PenAuditEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {LocalDateTimeMapper.class})
public interface PenDemogStudentMapper {
  PenDemogStudentMapper mapper = Mappers.getMapper(PenDemogStudentMapper.class);

  @Mapping(target = "studentTrueNo", ignore = true)
  @Mapping(target = "mergeToUserName", ignore = true)
  @Mapping(target = "mergeToDate", ignore = true)
  @Mapping(target = "mergeToCode", ignore = true)
  @Mapping(source = "usualMiddleNames", target = "usualMiddle")
  @Mapping(source = "usualLastName", target = "usualSurname")
  @Mapping(source = "usualFirstName", target = "usualGiven")
  @Mapping(source = "statusCode", target = "studStatus")
  @Mapping(source = "sexCode", target = "studSex")
  @Mapping(source = "pen", target = "studNo")
  @Mapping(source = "legalMiddleNames", target = "studMiddle")
  @Mapping(source = "legalLastName", target = "studSurname")
  @Mapping(source = "legalFirstName", target = "studGiven")
  @Mapping(source = "gradeCode", target = "grade")
  @Mapping(source = "genderCode", target = "studSex")
  @Mapping(source = "localID", target = "localID")
  @Mapping(source = "dob", target = "studBirth")
  @Mapping(source = "createUser", target = "createUser", defaultValue = "PEN_REPLICATION_API")
  @Mapping(source = "updateUser", target = "updateUser", defaultValue = "PEN_MIGRATION_API")
  PenDemographicsEntity toPenDemog(StudentCreate studentCreate);

  @Mapping(target = "activityDate", source = "createDate")
  @Mapping(target = "auditCode", source = "historyActivityCode")
  PenAuditEntity toPenAudit(StudentCreate studentCreate);
}
