package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Match reason codes.
 */
public enum MatchReasonCode {

  PENMATCH("PENMATCH", "AU"),

  PENCREATE("PENCREATE", "AU" ),

  DEMERGE("DEMERGE", "DM"),

  SPLIT("SPLIT", "AU" ),

  AU("AU", "AU"),

  DR("DR", "DR"),

  FR("FR", "FR"),

  IF("IF", "IF"),

  MI("MINISTRY", "MI"),

  SC("SC", "SC"),

  SD("SD", "SD"),

  SR("SR", "SR"),

  TX("TX", "TX");

  @Getter
  private final String oldCode;
  @Getter
  private final String prrCode;

  MatchReasonCode(String prrCode, String oldCode) {
    this.oldCode = oldCode;
    this.prrCode = prrCode;
  }
}
