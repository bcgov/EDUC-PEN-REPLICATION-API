package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.struct.saga.Saga;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SagaEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Saga mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface SagaMapper {
  /**
   * The constant mapper.
   */
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  /**
   * To struct saga.
   *
   * @param saga the saga
   * @return the saga
   */
  Saga toStruct(ca.bc.gov.educ.api.pen.replication.model.Saga saga);

  @Mapping(target = "sagaId", source = "saga.sagaId")
  SagaEvent toStruct(ca.bc.gov.educ.api.pen.replication.model.SagaEvent sagaEvent);
}
