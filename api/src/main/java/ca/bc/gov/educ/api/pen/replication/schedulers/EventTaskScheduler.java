package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
public class EventTaskScheduler {
  /**
   * The Saga orchestrators.
   */
  @Getter(PRIVATE)
  private final Map<SagaEnum, Orchestrator> sagaEnumOrchestratorMap = new EnumMap<>(SagaEnum.class);
  /**
   * The Saga repository.
   */
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;

  public EventTaskScheduler(final SagaRepository sagaRepository, final List<Orchestrator> orchestrators) {
    this.sagaRepository = sagaRepository;
    orchestrators.forEach(orchestrator -> this.sagaEnumOrchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", this.sagaEnumOrchestratorMap.keySet().stream().map(SagaEnum::getCode).collect(Collectors.joining(",")));
  }

  /**
   * Find and process uncompleted sagas.
   */
  @Scheduled(cron = "1 * * * * *") //
  @SchedulerLock(name = "REPLAY_UNCOMPLETED_SAGAS",
    lockAtLeastFor = "PT50S", lockAtMostFor = "PT55S")
  public void findAndProcessUncompletedSagas() {
    final List<Saga> sagas = this.getSagaRepository().findAllByStatusIn(this.getStatusFilters());
    if (!sagas.isEmpty()) {
      this.processUncompletedSagas(sagas);
    }
  }

  /**
   * Process uncompleted sagas.
   *
   * @param sagas the sagas
   */
  private void processUncompletedSagas(final List<Saga> sagas) {
    for (val saga : sagas) {
      if (saga.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(1))
        && this.getSagaEnumOrchestratorMap().containsKey(SagaEnum.getKeyFromValue(saga.getSagaName()))) {
        try {
          this.setRetryCountAndLog(saga);
          this.getSagaEnumOrchestratorMap().get(SagaEnum.getKeyFromValue(saga.getSagaName())).replaySaga(saga);
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, ex);
        } catch (final Exception e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, e);
        }
      }
    }
  }

  /**
   * Gets status filters.
   *
   * @return the status filters
   */
  public List<String> getStatusFilters() {
    final var statuses = new ArrayList<String>();
    statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    statuses.add(SagaStatusEnum.STARTED.toString());
    return statuses;
  }

  private void setRetryCountAndLog(final Saga saga) {
    Integer retryCount = saga.getRetryCount();
    if (retryCount == null || retryCount == 0) {
      retryCount = 1;
    } else {
      retryCount += 1;
    }
    saga.setRetryCount(retryCount);
    this.getSagaRepository().save(saga);
    LogHelper.logSagaRetry(saga);
  }
}
