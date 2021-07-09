package ca.bc.gov.educ.api.pen.replication.orchestrator.base;

import ca.bc.gov.educ.api.pen.replication.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.replication.model.Saga;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The interface Orchestrator.
 */
public interface Orchestrator {

  /**
   * Start saga.
   *
   * @param saga the saga data
   */
  void startSaga(Saga saga);

  /**
   * Gets saga name.
   *
   * @return the saga name
   */
  SagaEnum getSagaName();

  /**
   * Replay saga.
   *
   * @param saga the saga
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  void replaySaga(Saga saga) throws IOException, InterruptedException, TimeoutException;
}
