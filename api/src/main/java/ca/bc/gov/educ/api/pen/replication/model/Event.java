package ca.bc.gov.educ.api.pen.replication.model;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Pen match event.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "PEN_REPLICATION_EVENT")
@Data
@DynamicUpdate
public class Event {
  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;
  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;
  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER")
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "PEN_REPLICATION_EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID penReplicationEventId;
  /**
   * The Event id.
   */
  @Column(name = "EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID eventId;
  /**
   * The Event payload.
   */
  @NotNull(message = "eventPayload cannot be null")
  @Lob
  @Column(name = "EVENT_PAYLOAD")
  @ToString.Exclude
  private byte[] eventPayloadBytes;
  /**
   * The Event status.
   */
  @NotNull(message = "eventStatus cannot be null")
  @Column(name = "EVENT_STATUS")
  private String eventStatus;
  /**
   * The Event type.
   */
  @NotNull(message = "eventType cannot be null")
  @Column(name = "EVENT_TYPE")
  private String eventType;
  /**
   * The Event outcome.
   */
  @NotNull(message = "eventOutcome cannot be null.")
  @Column(name = "EVENT_OUTCOME")
  private String eventOutcome;

  /**
   * Gets event payload.
   *
   * @return the event payload
   */
  public String getEventPayload() {
    return new String(this.getEventPayloadBytes(), StandardCharsets.UTF_8);
  }

  /**
   * Sets event payload.
   *
   * @param eventPayload the event payload
   */
  public void setEventPayload(final String eventPayload) {
    this.setEventPayloadBytes(eventPayload.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * The type Student event builder.
   */
  public static class EventBuilder {
    /**
     * The Event payload bytes.
     */
    byte[] eventPayloadBytes;

    /**
     * Event payload student event . student event builder.
     *
     * @param eventPayload the event payload
     * @return the student event . student event builder
     */
    public Event.EventBuilder eventPayload(final String eventPayload) {
      this.eventPayloadBytes = eventPayload.getBytes(StandardCharsets.UTF_8);
      return this;
    }
  }
}
