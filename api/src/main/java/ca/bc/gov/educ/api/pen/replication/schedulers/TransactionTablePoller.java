package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.processor.TransactionTableRecordsProcessor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The type Transaction table poller.
 */
@Service
@Slf4j
public class TransactionTablePoller {

  private final TransactionTableRecordsProcessor processor;

  /**
   * Instantiates a new Transaction table poller.
   *
   * @param processor the processor
   */
  public TransactionTablePoller(final TransactionTableRecordsProcessor processor) {
    this.processor = processor;
  }


  /**
   * Find and process events.
   */
  @Scheduled(cron = "${cron.scheduled.process.records.transaction}") // every second
  @SchedulerLock(name = "PROCESS_ROWS_TRANSACTION", lockAtLeastFor = "${cron.scheduled.process.records.transaction.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.records.transaction.lockAtMostFor}")
  public void findAndProcessEvents() {
    LockAssert.assertLocked();
    this.processor.processUnprocessedRecords();
  }
}
