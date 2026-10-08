package ca.bc.gov.educ.api.pen.replication.model.dc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseCLOB {
  private List<GradStudentRecordCoursesLoad> studentCourseList;
}
