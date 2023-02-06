package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.DistrictMasterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring", uses = {UUIDMapper.class, LocalDateTimeMapper.class })
public interface DistrictMapper {

  DistrictMapper mapper = Mappers.getMapper(DistrictMapper.class);

  @Mapping(target = "distNo", ignore = true)
  @Mapping(target = "mailAddrValidStatus", ignore = true)
  @Mapping(target = "physAddrValidStatus", ignore = true)
  @Mapping(target = "mlas", ignore = true)
  @Mapping(target = "notes", ignore = true)
  void updateDistrictMaster(DistrictMasterEntity schoolMasterEntity, @MappingTarget DistrictMasterEntity targetEntity);
}
