package ca.bc.gov.educ.api.pen.replication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TraxStudentCoursePK implements Serializable {
    
    private String studNo;

    private String courseCode;

    private String courseLevel;
}
