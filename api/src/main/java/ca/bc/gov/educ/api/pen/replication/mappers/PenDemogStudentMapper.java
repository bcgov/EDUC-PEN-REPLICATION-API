package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Pen demog student mapper.
 */
@Mapper(uses = {LocalDateTimeMapper.class})
public interface PenDemogStudentMapper {
  /**
   * The constant mapper.
   */
  PenDemogStudentMapper mapper = Mappers.getMapper(PenDemogStudentMapper.class);

  /**
   * To pen demog pen demographics entity.
   *
   * @param studentCreate the student create
   * @return the pen demographics entity
   */
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
  @Mapping(source = "localID", target = "localID")
  @Mapping(source = "dob", target = "studBirth")
  @Mapping(source = "createUser", target = "createUser", defaultValue = ApplicationProperties.API_NAME)
  @Mapping(source = "updateUser", target = "updateUser", defaultValue = ApplicationProperties.API_NAME)
  PenDemographicsEntity toPenDemog(StudentCreate studentCreate);

  /**
   * To pen demog pen demographics entity.
   *
   * @param studentUpdate the student update
   * @return the pen demographics entity
   */
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
  @Mapping(source = "localID", target = "localID")
  @Mapping(source = "dob", target = "studBirth")
  @Mapping(source = "createUser", target = "createUser", defaultValue = ApplicationProperties.API_NAME)
  @Mapping(source = "updateUser", target = "updateUser", defaultValue = ApplicationProperties.API_NAME)
  PenDemographicsEntity toPenDemog(StudentUpdate studentUpdate);

}
