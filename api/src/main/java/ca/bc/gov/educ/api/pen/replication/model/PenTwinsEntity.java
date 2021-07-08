package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The type Pen twins entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PEN_TWINS")
public class PenTwinsEntity implements Serializable {

  private static final long serialVersionUID = -7707457912039224533L;
  /**
   * The Twin reason.
   */
  @Column(name = "TWIN_REASON", length = 2)
  String twinReason;
  /**
   * The Run date.
   */
  @Column(name = "RUN_DATE", length = 8)
  String runDate;
  /**
   * The Twin date.
   */
  @Column(name = "TWIN_DATE", length = 8)
  String twinDate;
  /**
   * The Twin user id.
   */
  @Column(name = "TWIN_USER_ID", length = 15)
  String twinUserID;
  @EmbeddedId
  private PenTwinsEntityID penTwinsEntityID;
}
