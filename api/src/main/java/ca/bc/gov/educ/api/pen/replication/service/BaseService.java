package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.constants.EventStatus;
import ca.bc.gov.educ.api.pen.replication.constants.TransactionStatus;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.time.LocalDateTime;


/**
 * The type Base service.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseService<T> implements EventService<T> {
  private final EventRepository eventRepository;
  private final EntityManagerFactory emf;

  /**
   * Instantiates a new Base service.
   *
   * @param eventRepository the event repository
   * @param emf             the emf
   */
  protected BaseService(final EventRepository eventRepository, final EntityManagerFactory emf) {
    this.eventRepository = eventRepository;
    this.emf = emf;
  }

  /**
   * Format date time string.
   *
   * @param activityDate the activity date
   * @return the string
   */
  protected String formatDateTime(String activityDate) {
    if (StringUtils.isBlank(activityDate)) {
      return activityDate;
    }
    activityDate = activityDate.replace("T", " ");
    if (activityDate.length() > 19) {
      activityDate = activityDate.substring(0, 19);
    }
    return activityDate;
  }

  /**
   * Update event.
   *
   * @param event the event
   */
  protected void updateEvent(final Event event) {
    this.eventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
      existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
      existingEvent.setUpdateDate(LocalDateTime.now());
      this.eventRepository.save(existingEvent);
    });
  }

  /**
   * Persist data.
   *
   * @param event the event
   * @param t     the t
   */
  protected void persistData(final Event event, final T t) {
    val em = this.emf.createEntityManager();
    val tx = em.getTransaction();

    try {
      tx.begin();
      this.buildAndExecutePreparedStatements(em, t);
      tx.commit();
      this.updateEvent(event);
    } catch (final Exception e) {
      log.error("Error occurred saving entity " + e.getMessage());
      if (tx.isActive()) {
        try {
          tx.rollback();
        } catch (final IllegalStateException | PersistenceException ex) {
          log.error("IllegalStateException | PersistenceException", ex);
        }
      }
    } finally {
      if (em.isOpen()) {
        em.close();
      }
    }
  }


  /**
   * if the event is part of orchestrator saga then we dont update here, it will be done in orchestrator flow.
   *
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param pen                           the pen
   * @return true if there is a running orchestrator saga for this one.
   */
  protected boolean isEventPartOfOrchestratorSaga(final PenDemogTransactionRepository penDemogTransactionRepository, final String pen) {
    return penDemogTransactionRepository.countPenDemogTransactionByPenAndTransactionStatus(pen, TransactionStatus.IN_PROGRESS.getCode()) > 0;
  }

  /**
   * if the event is part of orchestrator saga then we dont update here, it will be done in orchestrator flow.
   *
   * @param penTwinTransactionRepository the pen demog transaction repository
   * @param penTwin1                     the pen twin 1
   * @param penTwin2                     the pen twin 2
   * @return true if there is a running orchestrator saga for this one.
   */
  protected boolean isEventPartOfOrchestratorSaga(final PenTwinTransactionRepository penTwinTransactionRepository, final String penTwin1, final String penTwin2) {
    return penTwinTransactionRepository.countPenTwinTransactionByPenTwin1AndPenTwin2AndTransactionStatus(penTwin1, penTwin2, TransactionStatus.IN_PROGRESS.getCode()) > 0;
  }

  /**
   * Build and execute prepared statements.
   *
   * @param em the em
   * @param t  the t
   */
  protected abstract void buildAndExecutePreparedStatements(final EntityManager em, T t);
}
