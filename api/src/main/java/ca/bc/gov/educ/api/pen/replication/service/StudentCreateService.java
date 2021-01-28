package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenAuditEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pen.replication.repository.EventRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenAuditRepository;
import ca.bc.gov.educ.api.pen.replication.repository.PenDemogRepository;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.replication.constants.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.pen.replication.struct.EventType.CREATE_STUDENT;

/**
 * This class is responsible to get core student data
 */
@Service
@Slf4j
public class StudentCreateService implements EventService {
  private final PenDemogRepository penDemogRepository;
  private final EventRepository eventRepository;
  private final PenAuditRepository penAuditRepository;

  @Autowired
  public StudentCreateService(PenDemogRepository penDemogRepository, EventRepository eventRepository, PenAuditRepository penAuditRepository) {
    this.penDemogRepository = penDemogRepository;
    this.eventRepository = eventRepository;
    this.penAuditRepository = penAuditRepository;
  }

  @Override
  @Transactional(timeout = 30, propagation = Propagation.REQUIRES_NEW) // new transaction with 30 seconds time out.
  public <T extends BaseRequest> void processEvent(T request, Event event) {
    StudentCreate studentCreate = (StudentCreate) request;
    PenDemographicsEntity penDemographicsEntity = PenDemogStudentMapper.mapper.toPenDemog(studentCreate);
    PenAuditEntity penAuditEntity = PenDemogStudentMapper.mapper.toPenAudit(studentCreate);
    penAuditEntity.setActivityDate(formatDateTime(penAuditEntity.getActivityDate()));
    var existingPenDemogRecord = penDemogRepository.findById(StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10));
    if (existingPenDemogRecord.isPresent()) {
      var existingRecord = existingPenDemogRecord.get();
      BeanUtils.copyProperties(penDemographicsEntity, existingRecord);
      penDemogRepository.save(existingRecord);
    } else {
      penDemogRepository.save(penDemographicsEntity);
    }
    var existingAudits = penAuditRepository.findAllByPen(StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10));
    if (existingAudits.isEmpty()) {
      penAuditRepository.save(penAuditEntity);
    } else {
      boolean isRecordPresent = false;
      for (var existingAudit : existingAudits) {
        if (StringUtils.isNotBlank(existingAudit.getActivityDate())
            && StringUtils.isNotBlank(penAuditEntity.getActivityDate())
            && areDateTimeSame(existingAudit.getActivityDate(), penAuditEntity.getActivityDate())
            && StringUtils.equalsIgnoreCase(existingAudit.getAuditCode(), penAuditEntity.getAuditCode())) {
          isRecordPresent = true;
          break;
        }
      }
      if (!isRecordPresent) {
        penAuditRepository.save(penAuditEntity);
      }

    }
    var existingEvent = eventRepository.findById(event.getEventId());
    existingEvent.ifPresent(record -> {
      record.setEventStatus(PROCESSED.toString());
      record.setUpdateDate(LocalDateTime.now());
      eventRepository.save(record);
    });
  }

  private String formatDateTime(String activityDate) {
    if (StringUtils.isBlank(activityDate)) {
      return activityDate;
    }
    activityDate = activityDate.replace("T", " ");
    if (activityDate.length() > 19) {
      activityDate = activityDate.substring(0, 19);
    }
    return activityDate;
  }

  private boolean areDateTimeSame(String existingAuditActivityDate, String activityDate) {
    existingAuditActivityDate = existingAuditActivityDate.replace("T", " ");
    if (existingAuditActivityDate.length() > 19) {
      existingAuditActivityDate = existingAuditActivityDate.substring(0, 19);
    }
    activityDate = activityDate.replace("T", " ");
    if (activityDate.length() > 19) {
      activityDate = activityDate.substring(0, 19);
    }
    return StringUtils.equals(existingAuditActivityDate, activityDate);
  }


  @Override
  public String getEventType() {
    return CREATE_STUDENT.toString();
  }
}
