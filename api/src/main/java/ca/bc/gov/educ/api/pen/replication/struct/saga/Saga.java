package ca.bc.gov.educ.api.pen.replication.struct.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Saga {
  UUID sagaId;
  String sagaName;
  String sagaState;
  String payload;
  String status;
  String createUser;
  String updateUser;
  String createDate;
  String updateDate;
}
