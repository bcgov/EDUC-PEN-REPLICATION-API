package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeighborhoodLearning extends BaseRequest implements Serializable {

  private static final long serialVersionUID = 6118916290604876032L;

  private String neighborhoodLearningId;

  private String schoolId;

  @Size(max = 10)
  @NotNull(message = "neighborhoodLearningTypeCode cannot be null")
  private String neighborhoodLearningTypeCode;
}
