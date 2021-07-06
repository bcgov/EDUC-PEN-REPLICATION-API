package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.struct.saga.Saga;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface SagaMapper {
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  Saga toStruct(ca.bc.gov.educ.api.pen.replication.model.Saga saga);
}
