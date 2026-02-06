package ca.bc.gov.educ.api.pen.replication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "STUD_XCRSE")
public class TraxStudentCourseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "STUD_XCRSE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID studXcrseId;

    @Column(name = "STUD_NO", nullable = false)
    private String studNo;

    @Column(name = "CRSE_CODE", nullable = false)
    private String courseCode;

    @Column(name = "CRSE_LEVEL", nullable = false)
    private String courseLevel;

    @Column(name = "CRSE_SESSION", nullable = false)
    private String courseSession;
    
    @Column(name = "FINAL_LG")
    private String finalLetterGrade;

    @Column(name = "FINAL_PCT")
    private String finalPercentage;

    @Column(name = "NUM_CREDITS")
    private String numberOfCredits;

    @Column(name = "STUDY_TYPE")
    private String studyType;

    @Column(name = "USED_FOR_GRAD")
    private String usedForGrad;
    
}
