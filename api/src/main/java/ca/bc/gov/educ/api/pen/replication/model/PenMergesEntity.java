package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@Table(name = "PEN_MERGES")
@IdClass(PenMergePK.class)
public class PenMergesEntity implements Serializable {

  @Id
  @Column(name = "STUD_NO")
  private String studNo;

  @Id
  @Column(name = "STUD_TRUE_NO")
  private String studTrueNo;

  public PenMergePK getId() {
    return new PenMergePK(
      studNo,
      studTrueNo
    );
  }

  public void setId(PenMergePK id) {
    this.studNo = id.getStudNo();
    this.studTrueNo = id.getStudTrueNo();
  }

}

