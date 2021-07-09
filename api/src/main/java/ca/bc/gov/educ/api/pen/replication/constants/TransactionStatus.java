package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Transaction status.
 */
public enum TransactionStatus {
  /**
   * Pending transaction status.
   */
  PENDING("PEND"),
  /**
   * In progress transaction status.
   */
  IN_PROGRESS("IP"),
  /**
   * Complete transaction status.
   */
  COMPLETE("COMP");

  @Getter
  private final String code;

  TransactionStatus(final String code) {
    this.code = code;
  }
}
