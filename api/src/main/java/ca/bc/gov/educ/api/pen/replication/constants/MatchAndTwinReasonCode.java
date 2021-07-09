package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Match reason codes.
 */
public enum MatchAndTwinReasonCode {

  /**
   * Penmatch match and twin reason code.
   */
  PENMATCH("PENMATCH", "AU"),

  /**
   * Pencreate match and twin reason code.
   */
  PENCREATE("PENCREATE", "AU"),

  /**
   * Demerge match and twin reason code.
   */
  DEMERGE("DEMERGE", "DM"),

  /**
   * Split match and twin reason code.
   */
  SPLIT("SPLIT", "AU"),

  /**
   * Au match and twin reason code.
   */
  AU("AU", "AU"),

  /**
   * Dr match and twin reason code.
   */
  DR("DR", "DR"),

  /**
   * Fr match and twin reason code.
   */
  FR("FR", "FR"),

  /**
   * If match and twin reason code.
   */
  IF("IF", "IF"),

  /**
   * Mi match and twin reason code.
   */
  MI("MINISTRY", "MI"),

  /**
   * Sc match and twin reason code.
   */
  SC("SC", "SC"),

  /**
   * Sd match and twin reason code.
   */
  SD("SD", "SD"),

  /**
   * Sr match and twin reason code.
   */
  SR("SR", "SR"),

  /**
   * Tx match and twin reason code.
   */
  TX("TX", "TX");

  @Getter
  private final String oldCode;
  @Getter
  private final String prrCode;

  MatchAndTwinReasonCode(String prrCode, String oldCode) {
    this.oldCode = oldCode;
    this.prrCode = prrCode;
  }
}
