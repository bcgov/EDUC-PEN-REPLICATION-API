package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapperOracle.class})
@DecoratedWith(SchoolDecorator.class)
public interface SchoolMapper {

  SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

  @Mapping(source = "createUser", target = "createUser", defaultValue = "REPLICATION_API")
  @Mapping(source = "updateUser", target = "updateUser", defaultValue = "REPLICATION_API")
  SchoolMasterEntity toSchoolMaster(School school);

  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "createUsername", ignore = true)
  void updateSchoolMaster(SchoolMasterEntity schoolMasterEntity, @MappingTarget SchoolMasterEntity targetEntity);
}
