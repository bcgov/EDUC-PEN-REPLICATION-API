package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Transaction type.
 */
public enum TransactionType {
  /**
   * Create student transaction type.
   */
  CREATE_STUDENT("CS"),
  /**
   * Update student transaction type.
   */
  UPDATE_STUDENT("US"),
  /**
   * Create twins transaction type.
   */
  CREATE_TWINS("CT"),
  /**
   * Delete twins transaction type.
   */
  DELETE_TWINS("DT");

  @Getter
  private final String code;

  TransactionType(final String code) {
    this.code = code;
  }
}
