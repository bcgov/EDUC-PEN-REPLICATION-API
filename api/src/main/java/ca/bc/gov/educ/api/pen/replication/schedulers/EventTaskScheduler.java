package ca.bc.gov.educ.api.pen.replication.schedulers;

import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCourseUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
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
  /**
   * The Saga service.
   */
  @Getter(PRIVATE)
  private final SagaService sagaService;

  public EventTaskScheduler(final SagaRepository sagaRepository, final SagaService sagaService, final List<Orchestrator> orchestrators) {
    this.sagaRepository = sagaRepository;
    this.sagaService = sagaService;
    orchestrators.forEach(orchestrator -> this.sagaEnumOrchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", this.sagaEnumOrchestratorMap.keySet().stream().map(SagaEnum::getCode).collect(Collectors.joining(",")));
  }

  /**
   * Find and process uncompleted sagas.
   */
  @Scheduled(cron = "${cron.scheduled.process.uncompleted.saga}")
  @SchedulerLock(name = "REPLAY_UNCOMPLETED_SAGAS", lockAtLeastFor = "${cron.scheduled.process.uncompleted.saga.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.uncompleted.saga.lockAtMostFor}")
  public void findAndProcessUncompletedSagas() {
    final List<Saga> sagas = this.getSagaRepository().findTop500ByStatusInOrderByCreateDate(this.getStatusFilters());
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
      val compareDate = this.calculateDateBasedOnSagaName(saga);
      if (saga.getCreateDate().isBefore(compareDate)
        && this.getSagaEnumOrchestratorMap().containsKey(SagaEnum.getKeyFromValue(saga.getSagaName()))) {
        try {
          if (SagaStatusEnum.STARTED.toString().equals(saga.getStatus()) 
              && EventType.INITIATED.toString().equals(saga.getSagaState())) {
            this.processQueuedSaga(saga);
          } else {
            this.setRetryCountAndLog(saga);
            this.getSagaEnumOrchestratorMap().get(SagaEnum.getKeyFromValue(saga.getSagaName())).replaySaga(saga);
          }
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} ", saga, ex);
        } catch (final Exception e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} ", saga, e);
        }
      }
    }
  }

  private void processQueuedSaga(final Saga saga) {
    try {
      val sagaData = JsonUtil.getJsonObjectFromString(StudentCourseUpdateSagaData.class, saga.getPayload());

      if (sagaData != null) {
        val studentID = sagaData.getStudentID();
        
        // If this saga isn't the oldest for this student, don't run it
        val inProgressSagas = this.getSagaService().findInProgressStudentCourseUpdateSagasByStudentID(studentID);

        if (inProgressSagas.isEmpty() ||  inProgressSagas.get(0).getSagaId().equals(saga.getSagaId())) {
          this.getSagaEnumOrchestratorMap().get(SagaEnum.getKeyFromValue(saga.getSagaName())).startSaga(saga);
          log.info("Started queued saga {} for studentID {}", saga.getSagaId(), studentID);
        }
      }
    } catch (Exception e) {
      log.error("Error processing queued saga {}: {}", saga.getSagaId(), e.getMessage(), e);
    }
  }

  private LocalDateTime calculateDateBasedOnSagaName(final Saga saga) {
    if (SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA.getCode().equals(saga.getSagaName()) || SagaEnum.PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA.getCode().equals(saga.getSagaName())) {
      return LocalDateTime.now().minusMinutes(20);
    }
    return LocalDateTime.now().minusMinutes(1);
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
