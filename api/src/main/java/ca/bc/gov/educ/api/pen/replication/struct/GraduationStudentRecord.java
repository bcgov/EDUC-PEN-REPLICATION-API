package ca.bc.gov.educ.api.pen.replication.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraduationStudentRecord extends BaseRequest {

    private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private UUID schoolOfRecordId;
    private String schoolName;
    private String studentGrade;
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
    private UUID schoolAtGradId;
    private String schoolAtGradName;
    private String recalculateProjectedGrad;
    private Long batchId;
    private String consumerEducationRequirementMet;
    private String studentCitizenship;
    private String studentProjectedGradData;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    private Date adultStartDate;
}
