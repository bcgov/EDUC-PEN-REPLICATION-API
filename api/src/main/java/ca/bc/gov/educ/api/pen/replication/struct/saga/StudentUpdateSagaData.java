package ca.bc.gov.educ.api.pen.replication.struct.saga;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Student update saga data.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateSagaData implements Serializable {
  private static final long serialVersionUID = -6407984791836738557L;
  /**
   * The Pen demog transaction.
   */
  PenDemogTransaction penDemogTransaction;
  /**
   * The Student update.
   */
  StudentUpdate studentUpdate;
}
