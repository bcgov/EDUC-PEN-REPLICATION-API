package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Saga topics enum.
 */
public enum SagaTopicsEnum {
  /**
   * Pen replication student update saga topic saga topics enum.
   */
  PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC("PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC"),
  /**
   * Pen replication student create saga topic saga topics enum.
   */
  PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC("PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC"),
  /**
   * Pen services api topic saga topics enum.
   */
  PEN_SERVICES_API_TOPIC("PEN_SERVICES_API_TOPIC"),
  /**
   * Student api topic saga topics enum.
   */
  STUDENT_API_TOPIC("STUDENT_API_TOPIC"),
  /**
   * Pen replication possible match create saga topic saga topics enum.
   */
  PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA_TOPIC("PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA_TOPIC"),
  /**
   * Pen replication possible match delete saga topic saga topics enum.
   */
  PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA_TOPIC("PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA_TOPIC"),
  PEN_MATCH_API_TOPIC("PEN_MATCH_API_TOPIC");

  @Getter
  private final String code;

  SagaTopicsEnum(final String code) {
    this.code = code;
  }
}
