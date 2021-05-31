package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Match reason codes.
 */
public enum MatchReasonCode {

  PENMATCH("AU", "PENMATCH"),

  PENCREATE("AU", "PENCREATE"),

  DEMERGE("DM", "DEMERGE"),

  SPLIT("AU", "SPLIT"),

  AU("AU", "AU"),

  DR("DR", "DR"),

  FR("FR", "FR"),

  IF("IF", "IF"),

  MI("MI", "MINISTRY"),

  SC("SC", "SC"),

  SD("SD", "SD"),

  SR("SR", "SR"),

  TX("TX", "TX");

  @Getter
  private final String oldCode;
  @Getter
  private final String prrCode;

  MatchReasonCode(String oldCode, String prrCode) {
    this.oldCode = oldCode;
    this.prrCode = prrCode;
  }
}
