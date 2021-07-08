package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * The type Pen twins entity id.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PenTwinsEntityID implements Serializable {
  private static final long serialVersionUID = 7468807742772549780L;
  /**
   * The Pen twin 1.
   */
  @Column(name = "PEN_TWIN1", length = 10)
  String penTwin1;

  /**
   * The Pen twin 2.
   */
  @Column(name = "PEN_TWIN2", length = 10)
  String penTwin2;
}
