package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourse extends BaseRequest {

    private String studentID;
    private String id; //The property id is used only for update & rendering back on read.
    private String courseID;
    private String courseSession;
    private Integer interimPercent;
    private String interimLetterGrade;
    private Integer finalPercent;
    private String finalLetterGrade;
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String customizedCourseName;
    private String relatedCourseId;
    private StudentCourseExam courseExam;

}
