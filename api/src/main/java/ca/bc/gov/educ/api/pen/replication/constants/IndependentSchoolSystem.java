package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;


public enum IndependentSchoolSystem {

  IOSAS("IOSAS"),
  ISFS("ISFS");
  @Getter
  private final String code;

  IndependentSchoolSystem(final String code) {
    this.code = code;
  }

}
