package ca.bc.gov.educ.api.pen.replication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentRecordCoursesLoad {

  private String courseCode;
  private String courseLevel;
  private Integer credits;
  private String creditsUsedForGrad;
  private String finalLetterGrade;
  private Integer finalPercent;
  private String gradReqMet;
  private String sessionDate;
}
