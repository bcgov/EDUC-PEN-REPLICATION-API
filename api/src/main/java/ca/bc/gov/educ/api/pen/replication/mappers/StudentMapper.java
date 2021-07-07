package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@DecoratedWith(StudentDecorator.class)
public interface StudentMapper {
  /**
   * The constant mapper.
   */
  StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

  /**
   * To student create student create.
   *
   * @param penDemogTransaction the pen demog transaction
   * @return the student create
   */
  @Mapping(target = "usualMiddleNames", source = "usualMiddleName")
  @Mapping(target = "usualLastName", source = "usualSurname")
  @Mapping(target = "usualFirstName", source = "usualGivenName")
  @Mapping(target = "trueStudentID", ignore = true)
  @Mapping(target = "studentID", ignore = true)
  @Mapping(target = "statusCode", source = "status")
  @Mapping(target = "sexCode", source = "sex")
  @Mapping(target = "postalCode", source = "postal")
  @Mapping(target = "mincode", source = "penMinCode")
  @Mapping(target = "memo", ignore = true)
  @Mapping(target = "localID", source = "penLocalID")
  @Mapping(target = "legalMiddleNames", source = "middleName")
  @Mapping(target = "legalLastName", source = "surname")
  @Mapping(target = "legalFirstName", source = "givenName")
  @Mapping(target = "historyActivityCode", constant = "SLD")
  @Mapping(target = "gradeCode", source = "grade")
  @Mapping(target = "genderCode", source = "sex")
  @Mapping(target = "emailVerified", constant = "N")
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "dob", ignore = true)
  @Mapping(target = "deceasedDate", ignore = true)
  StudentCreate toStudentCreate(PenDemogTransaction penDemogTransaction);


  /**
   * To student create student create.
   *
   * @param studentUpdate the student update
   * @return the student create
   */
  StudentCreate toStudentCreate(StudentUpdate studentUpdate);

  /**
   * To student update student update.
   *
   * @param studentCreate the student create
   * @return the student update
   */
  StudentUpdate toStudentUpdate(StudentCreate studentCreate);

  @Mapping(target = "usualMiddleNames", source = "usualMiddleName")
  @Mapping(target = "usualLastName", source = "usualSurname")
  @Mapping(target = "usualFirstName", source = "usualGivenName")
  @Mapping(target = "trueStudentID", ignore = true)
  @Mapping(target = "studentID", ignore = true)
  @Mapping(target = "statusCode", source = "status")
  @Mapping(target = "sexCode", source = "sex")
  @Mapping(target = "postalCode", source = "postal")
  @Mapping(target = "mincode", source = "penMinCode")
  @Mapping(target = "memo", ignore = true)
  @Mapping(target = "localID", source = "penLocalID")
  @Mapping(target = "legalMiddleNames", source = "middleName")
  @Mapping(target = "legalLastName", source = "surname")
  @Mapping(target = "legalFirstName", source = "givenName")
  @Mapping(target = "historyActivityCode", constant = "SLD")
  @Mapping(target = "gradeCode", source = "grade")
  @Mapping(target = "genderCode", source = "sex")
  @Mapping(target = "emailVerified", constant = "N")
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "dob", ignore = true)
  @Mapping(target = "deceasedDate", ignore = true)
  StudentUpdate toStudent(PenDemogTransaction penDemogTransaction);

}
