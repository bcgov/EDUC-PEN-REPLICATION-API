package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Pen audit pk.
 */
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class PenAuditPK implements Serializable {
  /**
   * The Activity date.
   */
  String activityDate;
  /**
   * The Audit code.
   */
  String auditCode;
  /**
   * The Pen.
   */
  String pen;
}
