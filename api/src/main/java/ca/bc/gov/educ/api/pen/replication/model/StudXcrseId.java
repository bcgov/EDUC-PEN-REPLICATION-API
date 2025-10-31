package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Student XCRSE Id.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class StudXcrseId implements Serializable {
  @Column(name = "STUD_NO", nullable = false)
  private String studNo;

  @Column(name = "CRSE_CODE", nullable = false)
  private String courseCode;

  @Column(name = "CRSE_LEVEL", nullable = false)
  private String courseLevel;

  @Column(name = "CRSE_SESSION", nullable = false)
  private String courseSession;
}