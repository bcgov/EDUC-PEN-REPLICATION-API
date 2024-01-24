package ca.bc.gov.educ.api.pen.replication.constants;

import lombok.Getter;


public enum IndependentSchoolSystem {

  IOSAS("iosas"),
  ISFS("isfs");
  @Getter
  private final String code;

  IndependentSchoolSystem(final String code) {
    this.code = code;
  }

}
