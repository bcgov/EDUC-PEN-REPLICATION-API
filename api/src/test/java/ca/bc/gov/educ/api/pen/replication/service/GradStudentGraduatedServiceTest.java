package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.model.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.GRAD_STUDENT_GRADUATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class GradStudentGraduatedServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private GradStudentGraduatedService gradStudentGraduatedService;

  @Autowired
  private RestUtils restUtils;

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
            .build(),
        GradStudentRecordCoursesLoad.builder()
            .courseCode("EN")
            .courseLevel("12")
            .sessionDate("2025/06")
            .finalLetterGrade("B")
            .finalPercent(80)
            .credits(4)
            .build()
    );

    GradStatusEventPayloadDTO request = GradStatusEventPayloadDTO.builder()
        .pen(pen)
        .program("2023-EN")
        .programCompletionDate("2025/06")
        .studentGrade("12")
        .studentStatus("A")
        .updateUser("TEST")
        .build();
    when(this.restUtils.getGradStudentCourseListFromClobByPen(pen)).thenReturn(expectedCourses);

    String paddedPen = StringUtils.rightPad(request.getPen(), 10);
    var traxStudentRepository = this.penReplicationTestUtils.getTraxStudentRepository();
    traxStudentRepository.saveAndFlush(TraxStudentEntity.builder().studNo(paddedPen).build());

    final var event = TestUtils.createEvent(GRAD_STUDENT_GRADUATED.toString(), request,
        this.penReplicationTestUtils.getEventRepository());
    this.penReplicationTestUtils.getEventRepository().save(event);

    this.gradStudentGraduatedService.processEvent(request, event);

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
