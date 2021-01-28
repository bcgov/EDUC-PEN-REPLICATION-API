package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.mappers.PenDemogStudentMapper;
import ca.bc.gov.educ.api.pen.replication.model.Event;
import ca.bc.gov.educ.api.pen.replication.model.PenAuditEntity;
import ca.bc.gov.educ.api.pen.replication.model.PenAuditPK;
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
    var existingPenDemogRecord = penDemogRepository.findById(StringUtils.rightPad(penDemographicsEntity.getStudNo(), 10));
    if (existingPenDemogRecord.isPresent()) {
      var existingRecord = existingPenDemogRecord.get();
      BeanUtils.copyProperties(penDemographicsEntity, existingRecord);
      penDemogRepository.save(existingRecord);
    } else {
      penDemogRepository.save(penDemographicsEntity);
    }
    var existingAudit = penAuditRepository.findById(PenAuditPK.builder().auditCode(studentCreate.getHistoryActivityCode()).activityDate(studentCreate.getCreateDate()).pen(studentCreate.getPen()).build());
    if (existingAudit.isEmpty()) {
      penAuditRepository.save(penAuditEntity);
    }
    var existingEvent = eventRepository.findById(event.getEventId());
    existingEvent.ifPresent(record -> {
      record.setEventStatus(PROCESSED.toString());
      eventRepository.save(record);
    });
  }

  @Override
  public String getEventType() {
    return CREATE_STUDENT.toString();
  }
}
