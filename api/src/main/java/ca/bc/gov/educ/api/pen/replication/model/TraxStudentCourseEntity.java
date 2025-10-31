package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "STUD_XCRSE")
public class TraxStudentCourseEntity {

    @EmbeddedId
    private StudXcrseId studXcrseId;
    
    @Column(name = "FINAL_LG")
    private String finalLetterGrade;

    @Column(name = "FINAL_PCT")
    private String finalPercentage;

    @Column(name = "NUM_CREDITS")
    private String numberOfCredits;

    @Column(name = "STUDY_TYPE")
    private String studyType;

    @Column(name = "USED_FOR_GRAD")
    private String usedForGrad;
    
}
