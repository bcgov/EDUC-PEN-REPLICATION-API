package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring", uses = {UUIDMapper.class, LocalDateTimeMapper.class })
public interface AuthorityMapper {

  AuthorityMapper mapper = Mappers.getMapper(AuthorityMapper.class);

  @Mapping(target = "authNumber", ignore = true)
  @Mapping(target = "schoolUmbrellaGroupCode", ignore = true)
  @Mapping(target = "incorporationTypeCode", ignore = true)
  @Mapping(target = "incorporationNumber", ignore = true)
  @Mapping(target = "authSupplierNumber", ignore = true)
  @Mapping(target = "socialStatusCode", ignore = true)
  @Mapping(target = "lastAnnualFileDate", ignore = true)
  @Mapping(target = "incorporationDate", ignore = true)
  @Mapping(target = "privateActName", ignore = true)
  @Mapping(target = "vendorLocationCode", ignore = true)
  void updateAuthorityMaster(AuthorityMasterEntity schoolMasterEntity, @MappingTarget AuthorityMasterEntity targetEntity);
}
