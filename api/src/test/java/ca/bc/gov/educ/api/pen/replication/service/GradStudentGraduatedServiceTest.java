package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.model.dc.GradStudentCoursePayload;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.GraduationStudentRecord;
import ca.bc.gov.educ.api.pen.replication.support.TestUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.constants.EventType.GRAD_STUDENT_GRADUATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GradStudentGraduatedServiceTest extends BasePenReplicationAPITest {

  @Autowired
  private GradStudentGraduatedService gradStudentGraduatedService;

  @Autowired
  private RestUtils restUtils;

  @Test
  public void testProcessEvent_givenGRAD_STUDENT_GRADUATED_Event_shouldSaveCoursesInDB() throws IOException {
    String studentGradData;
    try (InputStream inputStream = getClass().getResourceAsStream("/studentGradData.json")) {
      assertThat(inputStream).as("studentGradData.json must exist in test resources").isNotNull();
      studentGradData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    GradStudentCoursePayload gradStudentCoursePayload = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(studentGradData, GradStudentCoursePayload.class);

    var expectedCourses = gradStudentCoursePayload.getStudentCourses().getStudentCourseList();

    GraduationStudentRecord request = new GraduationStudentRecord();
    request.setStudentID(UUID.randomUUID());
    request.setPen(expectedCourses.get(0).getPen());
    request.setStudentGradData(studentGradData);
    when(this.restUtils.getStudentPen(request.getStudentID().toString())).thenReturn(request.getPen());

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
  public void testParseStudentGradData_givenStudentGradDataJson_shouldReturnObject() throws IOException {

    //convert JSON into string representation to mimic what we would get in saga payload
    try (InputStream inputStream = getClass().getResourceAsStream("/studentGradData.json")) {
      assertThat(inputStream).as("studentGradData.json must exist in test resources").isNotNull();
      String studentGradData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

      GradStudentCoursePayload gradStudentCoursePayload = gradStudentGraduatedService.parseStudentGradData(studentGradData);

      assertThat(gradStudentCoursePayload).isNotNull();
    }
  }
}
