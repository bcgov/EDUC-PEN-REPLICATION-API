package ca.bc.gov.educ.api.pen.replication.repository;

import ca.bc.gov.educ.api.pen.replication.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

  /**
   * Find all by event status order by create date list.
   *
   * @param eventStatus the event status
   * @return the list
   */
  List<Event> findAllByEventStatusOrderByCreateDate(String eventStatus);
}
