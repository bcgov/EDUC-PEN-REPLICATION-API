package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapperHelper;
import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.model.SchoolMasterEntity;
import ca.bc.gov.educ.api.pen.replication.repository.SchoolMasterRepository;
import ca.bc.gov.educ.api.pen.replication.struct.School;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.CREATE_SCHOOL;

/**
 * The type School update service.
 */
@Service
@Slf4j
public class SchoolCreateService {

  private final SchoolMapperHelper schoolMapperHelper;
  private final SchoolMasterRepository schoolMasterRepository;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  public SchoolCreateService(final SchoolMapperHelper schoolMapperHelper, SchoolMasterRepository schoolMasterRepository) {
    this.schoolMapperHelper = schoolMapperHelper;
    this.schoolMasterRepository = schoolMasterRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public SchoolMasterEntity saveSchool(final School school) {
    if (StringUtils.isNotEmpty(school.getOpenedDate()) && dateTimeMapper.map(school.getOpenedDate()).isBefore(LocalDateTime.now())){
      var mincode = new Mincode();
      mincode.setSchlNo(school.getSchoolNumber());
      mincode.setDistNo(school.getMincode().substring(0,3));
      val existingSchoolMasterRecord = this.schoolMasterRepository.findById(mincode);
      if (!existingSchoolMasterRecord.isPresent()) {
        val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, true);
        schoolMasterRepository.save(newSchoolMaster);
      }
    }
    return null;
  }

}
