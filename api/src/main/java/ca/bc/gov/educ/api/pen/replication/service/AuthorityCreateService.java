package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthorityCreateService {

  private final AuthorityMapperHelper authorityMapperHelper;
  private final AuthorityMasterRepository authorityMasterRepository;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  public AuthorityCreateService(AuthorityMapperHelper authorityMapperHelper, AuthorityMasterRepository authorityMasterRepository) {
    this.authorityMapperHelper = authorityMapperHelper;
    this.authorityMasterRepository = authorityMasterRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AuthorityMasterEntity saveAuthority(final IndependentAuthority authority) {
    if (StringUtils.isNotEmpty(authority.getOpenedDate()) && dateTimeMapper.map(authority.getOpenedDate()).isBefore(LocalDateTime.now())){
      val existingSchoolMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());
      if (!existingSchoolMasterRecord.isPresent()) {
        var newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, true);
        return authorityMasterRepository.save(newAuthorityMaster);
      }
    }
    return null;
  }

}
