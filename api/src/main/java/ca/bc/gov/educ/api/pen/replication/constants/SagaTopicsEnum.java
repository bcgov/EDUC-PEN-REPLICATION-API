package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

public enum SagaTopicsEnum {
  PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC("PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC"),
  PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC("PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC");

  @Getter
  private final String code;

  SagaTopicsEnum(final String code) {
    this.code = code;
  }
}
