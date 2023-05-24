package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.AuthorityDatesProcessingHandler;
import ca.bc.gov.educ.api.pen.replication.service.SchoolDatesProcessingHandler;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
public class OpenAndCloseDateScheduler {

  private final RestUtils restUtils;

  private final SchoolDatesProcessingHandler schoolDatesProcessingHandler;

  private final AuthorityDatesProcessingHandler authorityDatesProcessingHandler;

  public OpenAndCloseDateScheduler(RestUtils restUtils, SchoolDatesProcessingHandler schoolDatesProcessingHandler, AuthorityDatesProcessingHandler authorityDatesProcessingHandler) {
    this.restUtils = restUtils;
    this.schoolDatesProcessingHandler = schoolDatesProcessingHandler;
    this.authorityDatesProcessingHandler = authorityDatesProcessingHandler;
  }

  @Scheduled(cron = "${cron.scheduled.process.events.school.dates}") // every 5 minutes
  @SchedulerLock(name = "PROCESS_SCHOOL_OPEN_AND_CLOSE_DATES", lockAtLeastFor = "${cron.scheduled.process.events.school.dates.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.school.dates.lockAtMostFor}")
  public void findAndProcessSchoolEvents() {
    LockAssert.assertLocked();
    log.info("Running scheduler for school open & close dates");
    final var results = this.restUtils.getSchoolsForOpeningAndClosing(UUID.randomUUID());
    if (!results.isEmpty()) {
      log.info("Found {} schools which need to be processed for open and close dates.", results.size());
      results.forEach(this.schoolDatesProcessingHandler::processSchoolForDates);
    }
  }

  @Scheduled(cron = "${cron.scheduled.process.events.authority.dates}") // every 5 minutes
  @SchedulerLock(name = "PROCESS_AUTHORITY_OPEN_AND_CLOSE_DATES", lockAtLeastFor = "${cron.scheduled.process.events.authority.dates.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.authority.dates.lockAtMostFor}")
  public void findAndProcessAuthorityEvents() {
    LockAssert.assertLocked();
    log.info("Running scheduler for authority open & close dates");
    final var results = this.restUtils.getAuthoritiesForOpeningAndClosing(UUID.randomUUID());
    if (!results.isEmpty()) {
      log.info("Found {} authorities which need to be processed for open and close dates.", results.size());
      results.forEach(this.authorityDatesProcessingHandler::processAuthorityForDates);
    }
  }

}
