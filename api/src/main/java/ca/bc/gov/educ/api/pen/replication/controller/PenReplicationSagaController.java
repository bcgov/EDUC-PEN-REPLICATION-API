package ca.bc.gov.educ.api.pen.replication.controller;

import ca.bc.gov.educ.api.pen.replication.endpoint.PenReplicationSagaEndpoint;
import ca.bc.gov.educ.api.pen.replication.exception.InvalidParameterException;
import ca.bc.gov.educ.api.pen.replication.filter.Associations;
import ca.bc.gov.educ.api.pen.replication.filter.BaseFilterSpecs;
import ca.bc.gov.educ.api.pen.replication.filter.FilterOperation;
import ca.bc.gov.educ.api.pen.replication.filter.SagaFilterSpecs;
import ca.bc.gov.educ.api.pen.replication.mappers.SagaMapper;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.Condition;
import ca.bc.gov.educ.api.pen.replication.struct.Search;
import ca.bc.gov.educ.api.pen.replication.struct.SearchCriteria;
import ca.bc.gov.educ.api.pen.replication.struct.ValueType;
import ca.bc.gov.educ.api.pen.replication.struct.saga.Saga;
import ca.bc.gov.educ.api.pen.replication.struct.saga.SagaEvent;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Pen replication saga controller.
 */
@RestController
@Slf4j
public class PenReplicationSagaController implements PenReplicationSagaEndpoint {

  private static final SagaMapper sagaMapper = SagaMapper.mapper;
  @Getter(PRIVATE)
  private final SagaService sagaService;
  /**
   * The saga filter specs.
   */
  @Getter(PRIVATE)
  private final SagaFilterSpecs sagaFilterSpecs;

  /**
   * Instantiates a new Pen replication saga controller.
   *
   * @param sagaService     the saga service
   * @param sagaFilterSpecs the saga filter specs
   */
  public PenReplicationSagaController(final SagaService sagaService, final SagaFilterSpecs sagaFilterSpecs) {
    this.sagaService = sagaService;
    this.sagaFilterSpecs = sagaFilterSpecs;
  }

  @Override
  public ResponseEntity<Saga> readSaga(final UUID sagaID) {
    return this.getSagaService().findSagaById(sagaID)
      .map(sagaMapper::toStruct)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Override
  @Transactional
  public ResponseEntity<Saga> updateSaga(final UUID sagaID, final Saga saga) {
    val sagaOptional = this.getSagaService().findSagaById(sagaID);
    if (sagaOptional.isPresent()) {
      val sagaFromDB = sagaOptional.get();
      sagaFromDB.setPayload(saga.getPayload());
      this.getSagaService().updateSagaRecord(sagaFromDB);
      return ResponseEntity.ok(sagaMapper.toStruct(sagaFromDB));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @Override
  @Transactional
  public ResponseEntity<SagaEvent> updateSagaEventState(final UUID sagaID, final UUID sagaEventID, final SagaEvent sagaEvent) {
    val sagaEventOptional = this.getSagaService().getSagaEventById(sagaEventID);
    if (sagaEventOptional.isPresent() && sagaID.equals(sagaEventOptional.get().getSaga().getSagaId())) {
      val sagaEventFromDB = sagaEventOptional.get();
      sagaEventFromDB.setSagaEventOutcome(sagaEvent.getSagaEventOutcome());
      sagaEventFromDB.setSagaEventResponse(sagaEvent.getSagaEventResponse());
      this.getSagaService().updateSagaEventRecord(sagaEventFromDB);
      return ResponseEntity.ok(sagaMapper.toStruct(sagaEventFromDB));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @Override
  public ResponseEntity<List<SagaEvent>> getSagaEventsBySagaID(final UUID sagaID) {
    val sagaOptional = this.getSagaService().findSagaById(sagaID);
    return sagaOptional.map(saga -> ResponseEntity.ok(this.getSagaService().findAllSagaStates(saga).stream()
      .map(SagaMapper.mapper::toStruct).collect(Collectors.toList())))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Override
  @Transactional
  public ResponseEntity<Void> deleteSagaEvent(final UUID sagaID, final UUID sagaEventID) {
    val sagaEventOptional = this.getSagaService().getSagaEventById(sagaEventID);
    if (sagaEventOptional.isPresent() && sagaID.equals(sagaEventOptional.get().getSaga().getSagaId())) {
      val sagaEventFromDB = sagaEventOptional.get();
      this.getSagaService().deleteSagaEvent(sagaEventFromDB);
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @Override
  public ResponseEntity<Page<Saga>> findAllSagas(final Integer pageNumber, final Integer pageSize, final String sortCriteriaJson, final String searchCriteriaListJson) {
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<ca.bc.gov.educ.api.pen.replication.model.Saga> sagaEntitySpecification = null;
    try {
      final var associationNames = this.getSortCriteria(sortCriteriaJson, JsonUtil.objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        final List<Search> searches = JsonUtil.objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        this.getAssociationNamesFromSearchCriterias(associationNames, searches);
        var i = 0;
        for (final var search : searches) {
          sagaEntitySpecification = this.getSpecifications(sagaEntitySpecification, i, search, associationNames, this.getSagaFilterSpecs());
          i++;
        }

      }
    } catch (final JsonProcessingException e) {
      throw new InvalidParameterException(e.getMessage());
    }
    return ResponseEntity.ok(this.getSagaService().findAll(sagaEntitySpecification, pageNumber, pageSize, sorts).map(SagaMapper.mapper::toStruct));
  }

  /**
   * Gets sort criteria.
   *
   * @param sortCriteriaJson the sort criteria json
   * @param objectMapper     the object mapper
   * @param sorts            the sorts
   * @return the sort criteria
   * @throws JsonProcessingException the json processing exception
   */
  public Associations getSortCriteria(final String sortCriteriaJson, final ObjectMapper objectMapper, final List<Sort.Order> sorts) throws JsonProcessingException {
    val associationNames = new Associations();
    if (StringUtils.isNotBlank(sortCriteriaJson)) {
      final Map<String, String> sortMap = objectMapper.readValue(sortCriteriaJson, new TypeReference<>() {
      });
      sortMap.forEach((k, v) -> {
        final var names = k.split("\\.");
        if (names.length > 1) {
          associationNames.getSortAssociations().add(names[0]);
        }

        if ("ASC".equalsIgnoreCase(v)) {
          sorts.add(new Sort.Order(Sort.Direction.ASC, k));
        } else {
          sorts.add(new Sort.Order(Sort.Direction.DESC, k));
        }
      });
    }
    return associationNames;
  }

  /**
   * Get association names from search criterias, like penRequestBatchEntity.mincode
   *
   * @param associationNames the associations
   * @param searches         the search criterias
   */
  public void getAssociationNamesFromSearchCriterias(final Associations associationNames, final List<Search> searches) {
    searches.forEach(search -> search.getSearchCriteriaList().forEach(criteria -> {
      final var names = criteria.getKey().split("\\.");
      if (names.length > 1) {
        associationNames.getSortAssociations().remove(names[0]);
        associationNames.getSearchAssociations().add(names[0]);
      }
    }));
  }

  /**
   * Gets specifications.
   *
   * @param <T>              the type parameter
   * @param specs            specs
   * @param specIndexNumber  the index of which spec this is in a list of search criteria
   * @param search           the search
   * @param associationNames the association names
   * @param filterSpecs      the filter specs
   * @return the specifications
   */
  public <T> Specification<T> getSpecifications(Specification<T> specs, final int specIndexNumber, final Search search, final Associations associationNames, final BaseFilterSpecs<T> filterSpecs) {
    if (specIndexNumber == 0) {
      specs = this.getEntitySpecification(search.getSearchCriteriaList(), associationNames, filterSpecs);
    } else {
      if (search.getCondition() == Condition.AND) {
        specs = specs.and(this.getEntitySpecification(search.getSearchCriteriaList(), associationNames, filterSpecs));
      } else {
        specs = specs.or(this.getEntitySpecification(search.getSearchCriteriaList(), associationNames, filterSpecs));
      }
    }
    return specs;
  }

  /**
   * Gets entity specification.
   *
   * @param criteriaList the criteria list
   * @return the entity specification
   */
  private <T> Specification<T> getEntitySpecification(final List<SearchCriteria> criteriaList, final Associations associationNames, final BaseFilterSpecs<T> filterSpecs) {
    Specification<T> entitySpecification = null;
    if (!criteriaList.isEmpty()) {
      var i = 0;
      for (final SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          final Specification<T> typeSpecification = this.getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteria.getValue(), criteria.getValueType(), associationNames, filterSpecs);
          entitySpecification = this.getSpecificationPerGroup(entitySpecification, i, criteria, typeSpecification);
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for", criteria.getKey(), criteria.getOperation().toString(), criteria.getValueType().toString());
        }
      }
    }
    return entitySpecification;
  }

  /**
   * Gets type specification.
   *
   * @param key             the key
   * @param filterOperation the filter operation
   * @param value           the value
   * @param valueType       the value type
   * @return the type specification
   */
  private <T> Specification<T> getTypeSpecification(final String key, final FilterOperation filterOperation, final String value, final ValueType valueType, final Associations associationNames, final BaseFilterSpecs<T> filterSpecs) {
    Specification<T> entitySpecification = null;
    switch (valueType) {
      case STRING:
        entitySpecification = filterSpecs.getStringTypeSpecification(key, value, filterOperation, associationNames);
        break;
      case DATE_TIME:
        entitySpecification = filterSpecs.getDateTimeTypeSpecification(key, value, filterOperation, associationNames);
        break;
      case LONG:
        entitySpecification = filterSpecs.getLongTypeSpecification(key, value, filterOperation, associationNames);
        break;
      case INTEGER:
        entitySpecification = filterSpecs.getIntegerTypeSpecification(key, value, filterOperation, associationNames);
        break;
      case DATE:
        entitySpecification = filterSpecs.getDateTypeSpecification(key, value, filterOperation, associationNames);
        break;
      case UUID:
        entitySpecification = filterSpecs.getUUIDTypeSpecification(key, value, filterOperation, associationNames);
        break;
      default:
        break;
    }
    return entitySpecification;
  }

  /**
   * Gets specification per group.
   *
   * @param entitySpecification the entity specification
   * @param specIndexNumber     the index of which spec this is in a list of search criteria
   * @param criteria            the criteria
   * @param typeSpecification   the type specification
   * @return the specification per group
   */
  private <T> Specification<T> getSpecificationPerGroup(Specification<T> entitySpecification, final int specIndexNumber, final SearchCriteria criteria, final Specification<T> typeSpecification) {
    if (specIndexNumber == 0) {
      entitySpecification = Specification.where(typeSpecification);
    } else {
      if (criteria.getCondition() == Condition.AND) {
        entitySpecification = entitySpecification.and(typeSpecification);
      } else {
        entitySpecification = entitySpecification.or(typeSpecification);
      }
    }
    return entitySpecification;
  }
}
