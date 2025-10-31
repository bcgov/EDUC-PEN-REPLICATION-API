package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    
    @Id
    @NotNull(message = "studNo cannot be null")
    @Column(name = "STUD_NO", unique = true, updatable = false)
    private String studNo;

    @Id
    @NotNull(message = "courseCode cannot be null")
    @Column(name = "CRSE_CODE")
    private String courseCode;

    @Id
    @NotNull(message = "courseLevel cannot be null")
    @Column(name = "CRSE_LEVEL")
    private String courseLevel;

    @Column(name = "CRSE_SESSION")
    private String courseSession;

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

    public TraxStudentCoursePK getId() {
        return new TraxStudentCoursePK(
                this.studNo, this.courseCode, this.courseLevel);
    }
    
    public void setId(TraxStudentCoursePK id) {
        this.studNo = id.getStudNo();
        this.courseCode = id.getCourseCode();
        this.courseLevel = id.getCourseLevel();
    }
    
}
