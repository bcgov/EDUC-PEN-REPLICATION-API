package ca.bc.gov.educ.api.pen.replication.utils;

import ca.bc.gov.educ.api.pen.replication.repository.PenDemogTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenTwinTransactionRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.SagaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The type Pen replication test utils.
 */
@Component
@Profile("test")
public class PenReplicationTestUtils {
  private final PenDemogTransactionRepository penDemogTransactionRepository;
  private final PenTwinTransactionRepository penTwinTransactionRepository;
  private final SagaRepository sagaRepository;
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Pen replication test utils.
   *
   * @param penDemogTransactionRepository the pen demog transaction repository
   * @param penTwinTransactionRepository  the pen twin transaction repository
   * @param sagaRepository                the saga repository
   * @param sagaEventRepository           the saga event repository
   */
  public PenReplicationTestUtils(final PenDemogTransactionRepository penDemogTransactionRepository, final PenTwinTransactionRepository penTwinTransactionRepository, final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository) {
    this.penDemogTransactionRepository = penDemogTransactionRepository;
    this.penTwinTransactionRepository = penTwinTransactionRepository;
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }

  /**
   * Clean db.
   */
  public void cleanDB() {
    this.sagaEventRepository.deleteAll();
    this.sagaRepository.deleteAll();
    this.penTwinTransactionRepository.deleteAll();
    this.penDemogTransactionRepository.deleteAll();
  }
}
