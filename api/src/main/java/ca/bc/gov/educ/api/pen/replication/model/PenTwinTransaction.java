package ca.bc.gov.educ.api.pen.replication.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The type Pen twin transaction.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "PEN_TWINS_TX")
public class PenTwinTransaction implements Serializable {

  private static final long serialVersionUID = 7763011484287937329L;

  /**
   * The Transaction id.
   */
  @Id
  @Column(name = "TX_ID", unique = true, updatable = false, length = 10)
  String transactionID;

  /**
   * The Transaction type.
   */
  @Column(name = "TX_TYPE", length = 6)
  String transactionType;

  /**
   * The Transaction status.
   */
  @Column(name = "TX_STATUS", length = 6)
  String transactionStatus;

  /**
   * The Transaction insert date time.
   */
  @Column(name = "TX_INSERT_DATE_TIME")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime transactionInsertDateTime;

  /**
   * The Transaction processed date time.
   */
  @Column(name = "TX_PROCESSED_DATE_TIME")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime transactionProcessedDateTime;

  /**
   * The Pen twin 1.
   */
  @Column(name = "PEN_TWIN1", length = 10)
  String penTwin1;

  /**
   * The Pen twin 2.
   */
  @Column(name = "PEN_TWIN2", length = 10)
  String penTwin2;


  /**
   * The Twin reason.
   */
  @Column(name = "TWIN_REASON", length = 2)
  String twinReason;

  /**
   * The Run date.
   */
  @Column(name = "RUN_DATE", length = 8)
  String runDate; // YYYYMMDD

  /**
   * The Twin user id.
   */
  @Column(name = "TWIN_USER_ID", length = 15)
  String twinUserID;

}
