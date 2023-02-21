package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
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


  public OpenAndCloseDateScheduler(RestUtils restUtils, SchoolDatesProcessingHandler schoolDatesProcessingHandler) {
    this.restUtils = restUtils;
    this.schoolDatesProcessingHandler = schoolDatesProcessingHandler;
  }

  @Scheduled(cron = "${cron.scheduled.process.events.school.dates}") // every 5 minutes
  @SchedulerLock(name = "PROCESS_SCHOOL_OPEN_AND_CLOSE_DATES", lockAtLeastFor = "${cron.scheduled.process.events.school.dates.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.school.dates.lockAtMostFor}")
  public void findAndProcessEvents() {
    LockAssert.assertLocked();
    log.info("Running scheduler for school open & close dates");
    final var results = this.restUtils.getSchoolsForOpeningAndClosing(UUID.randomUUID());
    if (!results.isEmpty()) {
      log.info("Found {} schools which need to be processed for open and close dates.", results.size());
      results.forEach(this.schoolDatesProcessingHandler::processSchoolForDates);
    }
  }

}
