package ca.bc.gov.educ.api.pen.replication.struct.saga;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Student create saga data.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCreateSagaData implements Serializable {

  private static final long serialVersionUID = 6922881826450048280L;
  /**
   * The Student create.
   */
  StudentCreate studentCreate;
  /**
   * The Pen demog transaction.
   */
  PenDemogTransaction penDemogTransaction;
}
