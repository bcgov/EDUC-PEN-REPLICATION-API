package ca.bc.gov.educ.api.pen.replication.struct.saga;


import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.PossibleMatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PossibleMatchSagaData implements Serializable {
  private static final long serialVersionUID = 3579826090373151319L;
  PenTwinTransaction penTwinTransaction;
  PossibleMatch possibleMatch;
}
