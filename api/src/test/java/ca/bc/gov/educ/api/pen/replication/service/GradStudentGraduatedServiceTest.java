package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.model.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.pen.replication.struct.SchoolTombstone;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.GRAD_STUDENT_GRADUATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class GradStudentGraduatedServiceTest extends BasePenReplicationAPITest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private GradStudentGraduatedService gradStudentGraduatedService;

  @Autowired
  private RestUtils restUtils;

  private Object[][] studyTypeParameters() {
    return new Object[][]{
        {null, null},
        {"", null},
        {"   ", null},
        {"3", " 3"},
        {" 3 ", " 3"},
        {"3, 203, 204", " 3"},
        {"203, 3, 204", " 3"},
        {"203, 204, 3", " 3"},
        {"203, 4, 204", null},
        {"23, 24, 25", null},
        {"13, 30, 203", null},
        {",,,", null},
        {", , ,", null},
        {"203,,3,,", " 3"},
        {"203,,204,,", null}
    };
  }

  @Test
  @Parameters(method = "studyTypeParameters")
  public void testDetermineStudyType_givenGradReqMet_shouldReturnExpectedStudyType(String gradReqMet, String expectedStudyType) {
    assertThat(this.gradStudentGraduatedService.determineStudyType(gradReqMet)).isEqualTo(expectedStudyType);
  }

  @Test
  public void testProcessEvent_givenGRAD_STUDENT_GRADUATED_Event_shouldSaveCoursesInDB() throws JsonProcessingException {
    String pen = "123456789";
    List<GradStudentRecordCoursesLoad> expectedCourses = List.of(
        GradStudentRecordCoursesLoad.builder()
            .courseCode("MATH")
            .courseLevel("12")
            .sessionDate("2025/06")
            .finalLetterGrade("A")
            .finalPercent(92)
            .credits(4)
            .gradReqMet("3, 203, 204")
            .build(),
        GradStudentRecordCoursesLoad.builder()
            .courseCode("EN")
            .courseLevel("12")
            .sessionDate("2025/06")
            .finalLetterGrade("B")
            .finalPercent(80)
            .credits(4)
            .gradReqMet("")
            .build()
    );

    GradStatusEventPayloadDTO request = GradStatusEventPayloadDTO.builder()
        .pen(pen)
        .program("2023-EN")
        .programCompletionDate("2012-06-01 00:00:00.000")
        .studentGrade("12")
        .studentStatus("A")
        .schoolOfRecordId(UUID.randomUUID())
        .schoolAtGradId(UUID.randomUUID())
        .updateUser("TEST")
        .build();
    var schoolOfRecord = new SchoolTombstone();
    schoolOfRecord.setMincode("12345678");
    var schoolAtGrad = new SchoolTombstone();
    schoolAtGrad.setMincode("87654321");
    when(this.restUtils.getSchoolBySchoolID(request.getSchoolOfRecordId().toString())).thenReturn(Optional.of(schoolOfRecord));
    when(this.restUtils.getSchoolBySchoolID(request.getSchoolAtGradId().toString())).thenReturn(Optional.of(schoolAtGrad));
    when(this.restUtils.getGradStudentCourseListFromClobByPen(pen)).thenReturn(expectedCourses);

    String paddedPen = StringUtils.rightPad(request.getPen(), 10);
    var traxStudentRepository = this.penReplicationTestUtils.getTraxStudentRepository();
    traxStudentRepository.saveAndFlush(TraxStudentEntity.builder().studNo(paddedPen).build());

    final var event = TestUtils.createEvent(GRAD_STUDENT_GRADUATED.toString(), request,
        this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);

    this.gradStudentGraduatedService.processEvent(request, event);

    var savedStudent = traxStudentRepository.findById(paddedPen).orElseThrow();
    assertThat(savedStudent.getMincode()).isEqualTo("12345678");
    assertThat(savedStudent.getMincodeGrad()).isEqualTo("87654321");
    assertThat(savedStudent.getGradDate()).isEqualTo(201206);
    assertThat(savedStudent.getGradReqtYear()).isEqualTo("2023");

    var traxStudentCourseRespository = this.penReplicationTestUtils.getTraxStudentCourseRespository();
    var savedCourses = traxStudentCourseRespository.findAllByStudNo(request.getPen());

    assertThat(savedCourses.size()).isEqualTo(expectedCourses.size());
    assertThat(this.penReplicationTestUtils.getEventRepository().findByEventId(event.getEventId()))
        .hasValueSatisfying(savedEvent -> assertThat(savedEvent.getEventStatus()).isEqualTo(PROCESSED.toString()));

  }

  @Test
  public void testProcessEvent_givenStudentNotFoundInGradStudentAPI_shouldPropagateNotFound() throws JsonProcessingException {
    String pen = "123456789";
    GradStatusEventPayloadDTO request = GradStatusEventPayloadDTO.builder()
        .pen(pen)
        .build();
    var notFound = WebClientResponseException.create(404, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
    when(this.restUtils.getGradStudentCourseListFromClobByPen(pen)).thenThrow(notFound);

    final var event = TestUtils.createEvent(GRAD_STUDENT_GRADUATED.toString(), request,
        this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);
    String originalEventStatus = event.getEventStatus();

    assertThatThrownBy(() -> this.gradStudentGraduatedService.processEvent(request, event))
        .isSameAs(notFound)
        .isInstanceOf(WebClientResponseException.NotFound.class);

    assertThat(this.penReplicationTestUtils.getTraxStudentCourseRespository().findAllByStudNo(pen)).isEmpty();
    assertThat(this.penReplicationTestUtils.getEventRepository().findByEventId(event.getEventId()))
        .hasValueSatisfying(savedEvent -> assertThat(savedEvent.getEventStatus()).isEqualTo(originalEventStatus));
  }
}
