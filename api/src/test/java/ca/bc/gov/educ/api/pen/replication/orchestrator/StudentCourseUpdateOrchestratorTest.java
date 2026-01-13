package ca.bc.gov.educ.api.pen.replication.orchestrator;

import ca.bc.gov.educ.api.pen.replication.BasePenReplicationAPITest;
import ca.bc.gov.educ.api.pen.replication.constants.*;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.model.Saga;
import ca.bc.gov.educ.api.pen.replication.model.StudXcrseId;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentCourseEntity;
import ca.bc.gov.educ.api.pen.replication.model.TraxStudentEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.service.SagaService;
import ca.bc.gov.educ.api.pen.replication.service.TraxStudentCourseService;
import ca.bc.gov.educ.api.pen.replication.service.TraxStudentService;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.struct.GradCourseCode;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCourse;
import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.struct.saga.StudentCourseUpdateSagaData;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.replication.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.replication.constants.SagaStatusEnum.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * The type Student course update orchestrator test.
 */
public class StudentCourseUpdateOrchestratorTest extends BasePenReplicationAPITest {
  private final String studentPEN = "123456789";
  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;
  @Autowired
  private SagaService sagaService;

  @Autowired
  private RestUtils restUtils;

  @SpyBean
  private TraxStudentCourseService traxStudentCourseService;

  @SpyBean
  private TraxStudentService traxStudentService;

  @Autowired
  private StudentCourseUpdateOrchestrator orchestrator;
  @Autowired
  private MessagePublisher messagePublisher;
  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private StudentCourseUpdateSagaData sagaData;
  /**
   * The Student id.
   */
  private String studentID;
  /**
   * The Course id 1.
   */
  private String courseID1;
  /**
   * The Course id 2.
   */
  private String courseID2;

  /**
   * Sets up.
   *
   * @throws IOException the io exception
   */
  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    this.studentID = UUID.randomUUID().toString();
    this.courseID1 = UUID.randomUUID().toString();
    this.courseID2 = UUID.randomUUID().toString();

    final List<StudentCourse> studentCourses = new ArrayList<>();
    final StudentCourse course1 = StudentCourse.builder()
      .courseID(this.courseID1)
      .courseSession("2023-09")
      .finalPercent(85)
      .finalLetterGrade("A")
      .credits(4)
      .build();
    final StudentCourse course2 = StudentCourse.builder()
      .courseID(this.courseID2)
      .courseSession("2023-09")
      .finalPercent(90)
      .finalLetterGrade("A+")
      .credits(3)
      .build();
    studentCourses.add(course1);
    studentCourses.add(course2);

    this.sagaData = StudentCourseUpdateSagaData.builder()
      .studentID(this.studentID)
      .studentCourses(studentCourses)
      .build();

    this.saga = this.sagaService.createSagaRecordInDB(
      SagaEnum.PEN_REPLICATION_STUDENT_COURSE_UPDATE_SAGA.getCode(),
      "Test",
      JsonUtil.objectMapper.writeValueAsString(this.sagaData));
  }

  /**
   * Test prepare student course update given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testPrepareStudentCourseUpdate_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    when(this.restUtils.getStudentPen(this.studentID)).thenReturn(this.studentPEN);

    final TraxStudentEntity traxStudent = TraxStudentEntity.builder()
      .studNo(String.format("%-10s", this.studentPEN))
      .build();
    when(this.traxStudentService.findTraxStudentByPen(anyString())).thenReturn(Optional.of(traxStudent));

    final List<TraxStudentCourseEntity> existingCourses = new ArrayList<>();
    final TraxStudentCourseEntity existingCourse = TraxStudentCourseEntity.builder()
      .studXcrseId(StudXcrseId.builder()
        .studNo(this.studentPEN)
        .courseCode("MATH")
        .courseLevel("11")
        .courseSession("2022-09")
        .build())
      .studyType("REG")
      .usedForGrad("Y")
      .build();
    existingCourses.add(existingCourse);
    when(this.traxStudentCourseService.findTraxStudentCoursesByPen(this.studentPEN)).thenReturn(existingCourses);

    final GradCourseCode course1 = new GradCourseCode();
    course1.setCourseID(this.courseID1);
    course1.setExternalCode("MATH-11");
    when(this.restUtils.getCoreg39CourseByID(this.courseID1)).thenReturn(Optional.of(course1));

    final GradCourseCode course2 = new GradCourseCode();
    course2.setCourseID(this.courseID2);
    course2.setExternalCode("ENG1-12");
    when(this.restUtils.getCoreg39CourseByID(this.courseID2)).thenReturn(Optional.of(course2));

    final var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(PREPARE_STUDENT_COURSE_UPDATE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSE_UPDATE_PREPARED);
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(PREPARE_STUDENT_COURSE_UPDATE.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  /**
   * Test prepare student course update given event and saga data and student not found should throw exception.
   */
  @Test
  public void testPrepareStudentCourseUpdate_givenEventAndSagaDataAndStudentNotFound_shouldThrowException() {
    when(this.restUtils.getStudentPen(this.studentID)).thenReturn(this.studentPEN);
    when(this.traxStudentService.findTraxStudentByPen(anyString())).thenReturn(Optional.empty());

    final var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .build();
    assertThatThrownBy(() -> this.orchestrator.handleEvent(event))
      .isInstanceOf(PenReplicationAPIRuntimeException.class)
      .hasMessageContaining("Student not found in TRAX with ID: " + this.studentPEN);
  }

  /**
   * Test delete student courses given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testDeleteStudentCourses_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    this.sagaData.setStudentPEN(this.studentPEN);
    this.saga.setPayload(JsonUtil.objectMapper.writeValueAsString(this.sagaData));
    this.sagaService.updateAttachedEntityDuringSagaProcess(this.saga);

    final var event = Event.builder()
      .eventType(PREPARE_STUDENT_COURSE_UPDATE)
      .eventOutcome(EventOutcome.STUDENT_COURSE_UPDATE_PREPARED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.traxStudentCourseService, times(1)).deleteTraxStudentCourses(this.studentPEN);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_STUDENT_COURSES);
    assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSES_DELETED);
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(DELETE_STUDENT_COURSES.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(PREPARE_STUDENT_COURSE_UPDATE.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSE_UPDATE_PREPARED.toString());
  }

  /**
   * Test save student courses given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testSaveStudentCourses_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    this.sagaData.setStudentPEN(this.studentPEN);
    final List<TraxStudentCourseEntity> newCourses = new ArrayList<>();
    final TraxStudentCourseEntity courseEntity = TraxStudentCourseEntity.builder()
      .studXcrseId(StudXcrseId.builder()
        .studNo(this.studentPEN)
        .courseCode("MATH")
        .courseLevel("11")
        .courseSession("2023-09")
        .build())
      .finalLetterGrade("A")
      .finalPercentage("85")
      .numberOfCredits("4")
      .build();
    newCourses.add(courseEntity);
    this.sagaData.setNewCourses(newCourses);
    this.saga.setPayload(JsonUtil.objectMapper.writeValueAsString(this.sagaData));
    this.sagaService.updateAttachedEntityDuringSagaProcess(this.saga);

    final var event = Event.builder()
      .eventType(DELETE_STUDENT_COURSES)
      .eventOutcome(EventOutcome.STUDENT_COURSES_DELETED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.traxStudentCourseService, times(1)).saveTraxStudentCourses(any());
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(SAVE_STUDENT_COURSES);
    assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSES_SAVED);
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(SAVE_STUDENT_COURSES.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(DELETE_STUDENT_COURSES.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSES_DELETED.toString());
  }

  /**
   * Test mark saga complete given event and saga data should post event to saga topic.
   *
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  @Test
  public void testMarkSagaComplete_givenEventAndSagaData_shouldPostEventToSagaTopic() throws IOException, InterruptedException, TimeoutException {
    final var event = Event.builder()
      .eventType(SAVE_STUDENT_COURSES)
      .eventOutcome(EventOutcome.STUDENT_COURSES_SAVED)
      .sagaId(this.saga.getSagaId())
      .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe().getCode()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.SAGA_COMPLETED);
    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(COMPLETED.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(SAVE_STUDENT_COURSES.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_COURSES_SAVED.toString());
  }
}
