package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Transaction status.
 */
public enum TransactionStatus {
  /**
   * Pending transaction status.
   */
  PENDING("NEW"), // New transaction record created from SLD
  /**
   * In progress transaction status.
   */
  IN_PROGRESS("INPROG"), // Transaction record is in progress in PRR
  /**
   * Complete transaction status.
   */
  COMPLETE("REPLIC"); // Transaction record has been replicated

  @Getter
  private final String code;

  TransactionStatus(final String code) {
    this.code = code;
  }
}
