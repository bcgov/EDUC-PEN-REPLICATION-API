package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCreateSagaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The type Student create orchestrator.
 */
@Component
@Slf4j
public class StudentCreateOrchestrator extends BaseOrchestrator<StudentCreateSagaData> {
  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  protected StudentCreateOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, StudentCreateSagaData.class, "REPLICATION_STUDENT_CREATE_SAGA", "REPLICATION_STUDENT_CREATE_SAGA_TOPIC");
  }

  @Override
  public void populateStepsToExecuteMap() {

  }
}
