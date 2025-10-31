package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseExam extends BaseRequest {

    private Integer schoolPercentage;
    private Integer bestSchoolPercentage;
    private Integer bestExamPercentage;
    private String specialCase;

    private UUID id;
    private Integer examPercentage;
    private String toWriteFlag;
    private String wroteFlag;

}
