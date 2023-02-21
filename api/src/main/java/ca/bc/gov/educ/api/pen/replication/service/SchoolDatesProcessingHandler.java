package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapper;
import ca.bc.gov.educ.api.pen.replication.mappers.SchoolMapperHelper;
import ca.bc.gov.educ.api.pen.replication.model.Mincode;
import ca.bc.gov.educ.api.pen.replication.repository.SchoolMasterRepository;
import ca.bc.gov.educ.api.pen.replication.struct.School;
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
public class SchoolDatesProcessingHandler {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(2).build();
  private final SchoolMapperHelper schoolMapperHelper;
  private final SchoolMasterRepository schoolMasterRepository;
  private static final SchoolMapper schoolMapper = SchoolMapper.mapper;
  public SchoolDatesProcessingHandler(SchoolMapperHelper schoolMapperHelper, SchoolMasterRepository schoolMasterRepository) {
    this.schoolMapperHelper = schoolMapperHelper;
    this.schoolMasterRepository = schoolMasterRepository;
  }

  public void processSchoolForDates(@NonNull final School school) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {
      log.info("Processing school for dates with mincode {} :: payload is: {}",school.getMincode(), school);
      var mincode = new Mincode();
      mincode.setSchlNo(school.getSchoolNumber());
      mincode.setDistNo(school.getMincode().substring(0,3));
      val existingSchoolMasterRecord = this.schoolMasterRepository.findById(mincode);

      if (existingSchoolMasterRecord.isPresent()) {
        log.info("Processing existing school for dates with ID {}", school.getSchoolId());
        val existingSchoolMaster = existingSchoolMasterRecord.get();
        ReplicationUtils.setCloseDateIfRequired(school, existingSchoolMaster);
        val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, false);
        schoolMapper.updateSchoolMaster(newSchoolMaster, existingSchoolMaster);
        schoolMasterRepository.save(existingSchoolMaster);
      } else {
        log.info("Processing new school for dates with mincode {}", school.getMincode());
        ReplicationUtils.setCloseDateIfRequired(school, null);
        // School needs to be created
        val newSchoolMaster = schoolMapperHelper.toSchoolMaster(school, true);
        schoolMasterRepository.save(newSchoolMaster);
      }
    });

  }
}
