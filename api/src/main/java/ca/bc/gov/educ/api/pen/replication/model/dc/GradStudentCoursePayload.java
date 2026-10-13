package ca.bc.gov.educ.api.pen.replication.model.dc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradStudentCoursePayload {
  private StudentCourseCLOB studentCourses;
}
