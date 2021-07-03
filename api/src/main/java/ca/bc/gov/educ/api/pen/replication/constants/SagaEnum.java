package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

public enum SagaEnum {
  PEN_REPLICATION_STUDENT_UPDATE_SAGA("PEN_REPLICATION_STUDENT_UPDATE_SAGA"),
  PEN_REPLICATION_STUDENT_CREATE_SAGA("PEN_REPLICATION_STUDENT_CREATE_SAGA");
  @Getter
  private final String code;

  SagaEnum(final String code) {
    this.code = code;
  }
}
