package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.AuthorityMapperHelper;
import ca.bc.gov.educ.api.pen.replication.repository.AuthorityMasterRepository;
import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import ca.bc.gov.educ.api.pen.replication.util.ReplicationUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;


/**
 * The type Choreograph event handler.
 */
@Component
@Slf4j
public class AuthorityDatesProcessingHandler {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(2).build();
  private final AuthorityMapperHelper authorityMapperHelper;
  private final AuthorityMasterRepository authorityMasterRepository;
  private static final AuthorityMapper authorityMapper = AuthorityMapper.mapper;

  public AuthorityDatesProcessingHandler(AuthorityMapperHelper authorityMapperHelper, AuthorityMasterRepository authorityMasterRepository) {
    this.authorityMapperHelper = authorityMapperHelper;
    this.authorityMasterRepository = authorityMasterRepository;
  }

  public void processAuthorityForDates(@NonNull final IndependentAuthority authority) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {
      log.info("Processing authority for dates with authority number {} :: payload is: {}", authority.getAuthorityNumber(), authority);
      val existingAuthorityMasterRecord = this.authorityMasterRepository.findById(authority.getAuthorityNumber());

      if (existingAuthorityMasterRecord.isPresent()) {
        log.info("Processing existing school for dates with ID {}", authority.getAuthorityNumber());
        val existingAuthorityMaster = existingAuthorityMasterRecord.get();
        ReplicationUtils.setCloseDateIfRequired(authority, existingAuthorityMaster);
        val newSchoolMaster = authorityMapperHelper.toAuthorityMaster(authority, false);
        authorityMapper.updateAuthorityMaster(newSchoolMaster, existingAuthorityMaster);
        authorityMasterRepository.save(existingAuthorityMaster);
      } else {
        log.info("Processing new authority for dates with authority number {}", authority.getAuthorityNumber());
        ReplicationUtils.setCloseDateIfRequired(authority, null);
        // School needs to be created
        val newAuthorityMaster = authorityMapperHelper.toAuthorityMaster(authority, true);
        authorityMasterRepository.save(newAuthorityMaster);
      }
    });

  }
}
