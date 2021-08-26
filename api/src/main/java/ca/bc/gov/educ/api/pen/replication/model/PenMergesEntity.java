package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;
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

  @Column(name = "STUD_NO")
  private String studNo;

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

