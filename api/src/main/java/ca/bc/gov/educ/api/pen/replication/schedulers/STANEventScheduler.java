package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.choreographer.StudentChoreographer;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.DB_COMMITTED;


/**
 * This class is responsible to check the PEN_MATCH_EVENT table periodically and publish messages to STAN, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class STANEventScheduler {

  /**
   * The Event repository.
   */
  private final EventRepository eventRepository;

  private final StudentChoreographer studentChoreographer;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param eventRepository      the event repository
   * @param studentChoreographer
   */
  public STANEventScheduler(EventRepository eventRepository, StudentChoreographer studentChoreographer) {
    this.eventRepository = eventRepository;
    this.studentChoreographer = studentChoreographer;
  }

  /**
   * Find and publish student events to stan.
   */
  @Scheduled(cron = "${cron.scheduled.process.events.stan}") // every 5 minutes
  @SchedulerLock(name = "PROCESS_CHOREOGRAPHED_EVENTS_FROM_STAN", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
  public void findAndPublishStudentEventsToSTAN() {
    LockAssert.assertLocked();
    var results = eventRepository.findAllByEventStatus(DB_COMMITTED.toString());
    if (!results.isEmpty()) {
      results.stream()
          .filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5)))
          .collect(Collectors.toList())
          .forEach(studentChoreographer::handleEvent);
    }
  }
}
