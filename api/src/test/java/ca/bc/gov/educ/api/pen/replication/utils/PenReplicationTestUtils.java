package ca.bc.gov.educ.api.pen.replication.utils;

import ca.bc.gov.educ.api.pen.replication.repository.*;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The type Pen replication test utils.
 */
@Component
@Profile("test")
@Getter
public class PenReplicationTestUtils {
  private final PenDemogRepository penDemogRepository;
  private final EventRepository eventRepository;
  private final PenDemogTransactionRepository penDemogTransactionRepository;
  private final PenTwinTransactionRepository penTwinTransactionRepository;
  private final SagaRepository sagaRepository;
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Pen replication test utils.
   *
   * @param penDemogRepository            the pen demog repository
   * @param eventRepository               the event repository
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param penTwinTransactionRepository  the pen twin transaction repository
   * @param sagaRepository                the saga repository
   * @param sagaEventRepository           the saga event repository
   */
  public PenReplicationTestUtils(PenDemogRepository penDemogRepository, EventRepository eventRepository, final PenDemogTransactionRepository penDemogTransactionRepository, final PenTwinTransactionRepository penTwinTransactionRepository, final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository) {
    this.penDemogRepository = penDemogRepository;
    this.eventRepository = eventRepository;
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }

  /**
   * Clean db.
   */
  public void cleanDB() {
    this.penDemogRepository.deleteAll();
    this.eventRepository.deleteAll();
    this.sagaEventRepository.deleteAll();
    this.sagaRepository.deleteAll();
    this.penTwinTransactionRepository.deleteAll();
    this.penDemogTransactionRepository.deleteAll();
  }
}
