package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;

/**
 * The enum Saga enum.
 */
public enum SagaEnum {
  /**
   * Pen replication student update saga enum.
   */
  PEN_REPLICATION_STUDENT_UPDATE_SAGA("PEN_REPLICATION_STUDENT_UPDATE_SAGA"),
  /**
   * Pen replication student create saga enum.
   */
  PEN_REPLICATION_STUDENT_CREATE_SAGA("PEN_REPLICATION_STUDENT_CREATE_SAGA"),
  /**
   * Pen replication possible match create saga enum.
   */
  PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA("PEN_REPLICATION_POSSIBLE_MATCH_CREATE_SAGA"),
  /**
   * Pen replication possible match delete saga enum.
   */
  PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA("PEN_REPLICATION_POSSIBLE_MATCH_DELETE_SAGA"),
  /**
   * Pen replication authority create saga enum.
   */
  PEN_REPLICATION_AUTHORITY_CREATE_SAGA("PEN_REPLICATION_AUTHORITY_CREATE_SAGA"),
  /**
   * Pen replication school create saga enum.
   */
  PEN_REPLICATION_SCHOOL_CREATE_SAGA("PEN_REPLICATION_SCHOOL_CREATE_SAGA"),
  /**
   * Pen replication district create saga enum.
   */
  PEN_REPLICATION_DISTRICT_CREATE_SAGA("PEN_REPLICATION_DISTRICT_CREATE_SAGA");
  @Getter
  private final String code;

  SagaEnum(final String code) {
    this.code = code;
  }

  public static SagaEnum getKeyFromValue(String value) {
    for (SagaEnum e : SagaEnum.values()) {
      if (value.equalsIgnoreCase(e.getCode())) {
        return e;
      }
    }
    return null;
  }
}
