package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Transaction type.
 */
public enum TransactionType {
  /**
   * Create student transaction type.
   */
  CREATE_STUDENT("INSERT"),
  /**
   * Update student transaction type.
   */
  UPDATE_STUDENT("UPDATE"),
  /**
   * Create twins transaction type.
   */
  CREATE_TWINS("INSERT"),
  /**
   * Delete twins transaction type.
   */
  DELETE_TWINS("DELETE");

  @Getter
  private final String code;

  TransactionType(final String code) {
    this.code = code;
  }
}
