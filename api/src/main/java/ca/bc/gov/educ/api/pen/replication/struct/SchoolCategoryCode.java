package ca.bc.gov.educ.api.pen.replication.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Grade code.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1700")
public class SchoolCategoryCode implements Serializable {
  private static final long serialVersionUID = 8115561492500492122L;

  String schoolCategoryCode;
  /**
   * The Label.
   */
  String label;
  /**
   * The Description.
   */
  String description;
  /**
   * The Legacy Code.
   */
  String legacyCode;
  /**
   * The Display order.
   */
  Integer displayOrder;
  /**
   * The Effective date.
   */
  String effectiveDate;
  /**
   * The Expiry date.
   */
  String expiryDate;
}
