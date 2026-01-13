package ca.bc.gov.educ.api.pen.replication.struct.saga;

import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * The type Student course update saga data.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseUpdateSagaData implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentPEN;
  private String studentID;
  private List<StudentCourse> studentCourses;
  private List<TraxStudentCourseEntity> newCourses;
}

