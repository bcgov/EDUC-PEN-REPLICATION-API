package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Event repository.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
  /**
   * Find by event id optional.
   *
   * @param eventId the event id
   * @return the optional
   */
  Optional<Event> findByEventId(UUID eventId);

  @Query(value = "select event.* from PEN_REPLICATION_EVENT event where event.EVENT_STATUS = :eventStatus " +
          "AND event.CREATE_DATE < :createDate " +
          "ORDER BY event.CREATE_DATE asc " +
          "FETCH FIRST :limit ROWS ONLY", nativeQuery=true)
  List<Event> findAllByEventStatusAndCreateDateBeforeOrderByCreateDate(String eventStatus, LocalDateTime createDate, int limit);

  @Transactional
  @Modifying
  @Query("delete from Event where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
