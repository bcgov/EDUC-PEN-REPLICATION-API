package ca.bc.gov.educ.api.pen.replication.struct.saga;

import ca.bc.gov.educ.api.pen.replication.struct.School;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchoolCreateSagaData implements Serializable {

  private static final long serialVersionUID = 6922881826450048280L;

  School school;
}
