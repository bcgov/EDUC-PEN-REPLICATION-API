package ca.bc.gov.educ.api.pen.replication.exception;

import lombok.Getter;

public enum BusinessError {
  EVENT_ALREADY_PERSISTED("Event with event id :: $? , is already persisted in DB, a duplicate message from STAN.");

  @Getter
  private final String code;

  BusinessError(String code) {
    this.code = code;

  }
}
