package ca.bc.gov.educ.api.pen.replication.model.dc;

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
  private String credits;
  private String creditsUsedForGrad;
  private String finalLetterGrade;
  private String finalPercent;
  private String gradReqMet;
  private String pen;
  private String sessionDate;
}
