package ca.bc.gov.educ.api.pen.replication.rest;

import ca.bc.gov.educ.api.pen.replication.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.replication.constants.EventType;
import ca.bc.gov.educ.api.pen.replication.exception.PenReplicationAPIRuntimeException;
import ca.bc.gov.educ.api.pen.replication.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String STUDENT_NOT_FOUND_FOR = "Student not found for , ";

  private static final String AUTHORITY_NOT_FOUND_FOR = "Authority not found for , ";
  private static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";
  private static final String INSTITUTE_API_TOPIC = "INSTITUTE_API_TOPIC";
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Map<String, SchoolCategoryCode> schoolCategoryCodesMap = new ConcurrentHashMap<>();
  private final ReadWriteLock schoolCategoryLock = new ReentrantReadWriteLock();
  private final Map<String, SchoolOrganizationCode> schoolOrganizationCodesMap = new ConcurrentHashMap<>();
  private final ReadWriteLock schoolOrganizationLock = new ReentrantReadWriteLock();
  private final Map<String, FacilityTypeCode> facilityTypeCodesMap = new ConcurrentHashMap<>();
  private final ReadWriteLock facilityTypeLock = new ReentrantReadWriteLock();
  private final Map<String, ProvinceCode> provinceCodesMap = new ConcurrentHashMap<>();
  private final ReadWriteLock provinceLock = new ReentrantReadWriteLock();
  private final Map<String, CountryCode> countryCodesMap = new ConcurrentHashMap<>();
  private final ReadWriteLock countryLock = new ReentrantReadWriteLock();
  private final WebClient webClient;
  @Getter
  private final ApplicationProperties props;
  @Value("${initialization.background.enabled}")
  private Boolean isBackgroundInitializationEnabled;

  /**
   * Instantiates a new Rest utils.
   *
   * @param messagePublisher the message publisher
   * @param webClient
   * @param props
   */
  @Autowired
  public RestUtils(final MessagePublisher messagePublisher, WebClient webClient, ApplicationProperties props) {
    this.messagePublisher = messagePublisher;
    this.webClient = webClient;
    this.props = props;
  }

  @PostConstruct
  public void init() {
    if (this.isBackgroundInitializationEnabled != null && this.isBackgroundInitializationEnabled) {
      ApplicationProperties.bgTask.execute(this::initialize);
    }
  }

  private void initialize() {
    this.setOrganizationCodesMap();
    this.setFacilityTypeCodesMap();
    this.setCategoryCodesMap();
    this.setProvinceCodesMap();
    this.setCountryCodesMap();
    log.info("Called institute api and loaded {} category codes", this.schoolCategoryCodesMap.values().size());
    log.info("Called institute api and loaded {} facility codes", this.facilityTypeCodesMap.values().size());
    log.info("Called institute api and loaded {} organization codes", this.schoolOrganizationCodesMap.values().size());
    log.info("Called institute api and loaded {} province codes", this.provinceCodesMap.values().size());
    log.info("Called institute api and loaded {} country codes", this.countryCodesMap.values().size());
  }

  /**
   * Gets students by id.
   *
   * @param studentIDs the student i ds
   * @return the students by id
   */
  @SneakyThrows({IOException.class, InterruptedException.class})
  @Retryable(value = {Exception.class}, exclude = {PenReplicationAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Map<String, Student> getStudentsByID(final List<String> studentIDs) {
    log.info("called STUDENT_API to get students :: {}", studentIDs);
    final var event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(UUID.randomUUID()).eventType(EventType.GET_STUDENTS).eventPayload(JsonUtil.getJsonStringFromObject(studentIDs)).build();
    try {
      val response = this.messagePublisher.requestMessage(STUDENT_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 5, TimeUnit.SECONDS).get();
      if (response == null || response.getData() == null || response.getData().length == 0) {
        log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
        throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs);
      }
      val responseEvent = JsonUtil.getJsonObjectFromByteArray(ca.bc.gov.educ.api.pen.replication.struct.Event.class, response.getData());
      log.info("got response from STUDENT_API  :: {}", responseEvent);
      if (responseEvent.getEventOutcome() == EventOutcome.STUDENTS_NOT_FOUND) {
        log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
        throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs);
      }
      final List<Student> students = this.objectMapper.readValue(responseEvent.getEventPayload(), new TypeReference<>() {
      });
      log.info("got response from STUDENT_API found :: {} students", students.size());
      return students.stream().collect(Collectors.toConcurrentMap(Student::getStudentID, Function.identity()));
    } catch (final ExecutionException e) {
      throw new PenReplicationAPIRuntimeException(STUDENT_NOT_FOUND_FOR + studentIDs + " :: " + e.getMessage());
    }
  }

  @SneakyThrows({IOException.class})
  @Retryable(value = {Exception.class}, exclude = {PenReplicationAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public IndependentAuthority getIndependentAuthorityByID(final String authorityID) {
    log.info("Called INSTITUTE_API to get authority :: {}", authorityID);
    final var event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(UUID.randomUUID()).eventType(EventType.GET_AUTHORITY).eventPayload(JsonUtil.getJsonStringFromObject(authorityID)).build();
    try {
      val response = this.messagePublisher.requestMessage(INSTITUTE_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 5, TimeUnit.SECONDS).get();
      if (response == null || response.getData() == null || response.getData().length == 0) {
        log.error("Authority not found :: {}, this should not have happened", authorityID);
        throw new PenReplicationAPIRuntimeException(AUTHORITY_NOT_FOUND_FOR + authorityID);
      }
      log.info("Response data is :: {}", response);
      val authority = JsonUtil.getJsonObjectFromByteArray(ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority.class, response.getData());
      log.info("Received response from INSTITUTE_API  :: {}", authority);
      if (authority == null) {
        log.error("Authority not found :: {}, this should not have happened", authorityID);
        throw new PenReplicationAPIRuntimeException(AUTHORITY_NOT_FOUND_FOR + authorityID);
      }
      log.info("Got response from INSTITUTE_API found authority :: {}", authority.getIndependentAuthorityId());
      return authority;
    }  catch (final InterruptedException e){
      Thread.currentThread().interrupt();
      throw new PenReplicationAPIRuntimeException(AUTHORITY_NOT_FOUND_FOR + authorityID + " :: " + e.getMessage());
    } catch (final Exception e) {
      throw new PenReplicationAPIRuntimeException(AUTHORITY_NOT_FOUND_FOR + authorityID + " :: " + e.getMessage());
    }
  }

  /**
   * Gets student pen.
   *
   * @param trueStudentID the true student id
   * @return the student pen
   */
  public String getStudentPen(final String trueStudentID) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(trueStudentID);
    return this.getStudentsByID(studentIDs).get(trueStudentID).getPen();
  }

  /**
   * Create student map from possible match map.
   *
   * @param possibleMatch the possible match
   * @return the map
   */
  public Map<String, Student> createStudentMapFromPossibleMatch(final PossibleMatch possibleMatch) {
    final List<String> studentIDs = new ArrayList<>();
    studentIDs.add(possibleMatch.getStudentID());
    studentIDs.add(possibleMatch.getMatchedStudentID());
    return this.getStudentsByID(studentIDs);
  }

  public void setOrganizationCodesMap() {
    val writeLock = this.schoolOrganizationLock.writeLock();
    try {
      writeLock.lock();
      this.schoolOrganizationCodesMap.clear();
      final List<SchoolOrganizationCode> organizationCodes = this.webClient.get().uri(this.props.getInstituteApiURL(), uri -> uri.path("/organization-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(SchoolOrganizationCode.class).collectList().block();
      if(organizationCodes != null) {
        this.schoolOrganizationCodesMap.putAll(organizationCodes.stream().collect(Collectors.toMap(SchoolOrganizationCode::getSchoolOrganizationCode, Function.identity())));
      }
    }
    catch (Exception ex) {
      log.error("Unable to load map cache organization codes {}", ex);
    }
    finally {
      writeLock.unlock();
    }
  }

  public Map<String,SchoolOrganizationCode> getSchoolOrganizationCodes() {
    if(this.schoolOrganizationCodesMap.isEmpty()) {
      setOrganizationCodesMap();
    }
    return this.schoolOrganizationCodesMap;
  }

  public void setCategoryCodesMap() {
    val writeLock = this.schoolCategoryLock.writeLock();
    try {
      writeLock.lock();
      this.schoolCategoryCodesMap.clear();
      final List<SchoolCategoryCode> categoryCodes = this.webClient.get().uri(this.props.getInstituteApiURL(), uri -> uri.path("/category-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(SchoolCategoryCode.class).collectList().block();
      if(categoryCodes != null) {
        this.schoolCategoryCodesMap.putAll(categoryCodes.stream().collect(Collectors.toMap(SchoolCategoryCode::getSchoolCategoryCode, Function.identity())));
      }
    }
    catch (Exception ex) {
      log.error("Unable to load map cache category codes {}", ex);
    }
    finally {
      writeLock.unlock();
    }
  }

  public Map<String,SchoolCategoryCode> getSchoolCategoryCodes() {
    if(this.schoolCategoryCodesMap.isEmpty()) {
      setCategoryCodesMap();
    }
    return this.schoolCategoryCodesMap;
  }

  public void setFacilityTypeCodesMap() {
    val writeLock = this.facilityTypeLock.writeLock();
    try {
      writeLock.lock();
      this.facilityTypeCodesMap.clear();
      final List<FacilityTypeCode> facilityTypeCodes = this.webClient.get().uri(this.props.getInstituteApiURL(), uri -> uri.path("/facility-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(FacilityTypeCode.class).collectList().block();
      if(facilityTypeCodes != null) {
        this.facilityTypeCodesMap.putAll(facilityTypeCodes.stream().collect(Collectors.toMap(FacilityTypeCode::getFacilityTypeCode, Function.identity())));
      }
    }
    catch (Exception ex) {
      log.error("Unable to load map cache facility type codes {}", ex);
    }
    finally {
      writeLock.unlock();
    }
  }

  public Map<String,FacilityTypeCode> getFacilityTypeCodes() {
    if(this.facilityTypeCodesMap.isEmpty()) {
      setFacilityTypeCodesMap();
    }
    return this.facilityTypeCodesMap;
  }

  public void setProvinceCodesMap() {
    val writeLock = this.provinceLock.writeLock();
    try {
      writeLock.lock();
      this.provinceCodesMap.clear();
      final List<ProvinceCode> provinceCodes = this.webClient.get().uri(this.props.getInstituteApiURL(), uri -> uri.path("/province-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(ProvinceCode.class).collectList().block();
      if(provinceCodes != null) {
        this.provinceCodesMap.putAll(provinceCodes.stream().collect(Collectors.toMap(ProvinceCode::getProvinceCode, Function.identity())));
      }
    }
    catch (Exception ex) {
      log.error("Unable to load map cache province type codes {}", ex);
    }
    finally {
      writeLock.unlock();
    }
  }

  public Map<String,ProvinceCode> getProvinceCodes() {
    if(this.provinceCodesMap.isEmpty()) {
      setFacilityTypeCodesMap();
    }
    return this.provinceCodesMap;
  }

  public void setCountryCodesMap() {
    val writeLock = this.countryLock.writeLock();
    try {
      writeLock.lock();
      this.countryCodesMap.clear();
      final List<CountryCode> countryCodes = this.webClient.get().uri(this.props.getInstituteApiURL(), uri -> uri.path("/country-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(CountryCode.class).collectList().block();
      if(countryCodes != null) {
        this.countryCodesMap.putAll(countryCodes.stream().collect(Collectors.toMap(CountryCode::getCountryCode, Function.identity())));
      }
    }
    catch (Exception ex) {
      log.error("Unable to load map cache country type codes {}", ex);
    }
    finally {
      writeLock.unlock();
    }
  }

  public Map<String,CountryCode> getCountryCodes() {
    if(this.countryCodesMap.isEmpty()) {
      setFacilityTypeCodesMap();
    }
    return this.countryCodesMap;
  }

  /**
   * Create student map from pen numbers map.
   *
   * @param pens   the pens
   * @param sagaId the saga id
   * @return the map
   */
  @SneakyThrows
  @Retryable(value = {Exception.class}, exclude = {PenReplicationAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Map<String, Student> createStudentMapFromPenNumbers(@NonNull final List<String> pens, final UUID sagaId) {
    final Map<String, Student> penStudentMap = new HashMap<>();
    for (val pen : pens) {
      val event = ca.bc.gov.educ.api.pen.replication.struct.Event.builder().sagaId(sagaId == null ? UUID.randomUUID() : sagaId).eventType(EventType.GET_STUDENT).eventPayload(pen).build();
      val response = this.messagePublisher.requestMessage(STUDENT_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 5, TimeUnit.SECONDS).get();
      if (response == null) {
        throw new PenReplicationAPIRuntimeException("Either NATS is down or api is down as response could not be received.");
      }
      if (response.getData() != null && response.getData().length > 0) {
        val student = JsonUtil.getJsonObjectFromByteArray(Student.class, response.getData());
        penStudentMap.put(pen, student);
      }
    }
    return penStudentMap;
  }


}
