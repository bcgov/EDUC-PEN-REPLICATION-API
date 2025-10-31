package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class StudentCourseUpdate {
    private String studentID;
    private List<StudentCourse> studentCourses;
}
