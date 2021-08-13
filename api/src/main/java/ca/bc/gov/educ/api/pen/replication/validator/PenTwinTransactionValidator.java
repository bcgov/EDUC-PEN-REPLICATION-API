package ca.bc.gov.educ.api.pen.replication.validator;

import ca.bc.gov.educ.api.pen.replication.constants.MatchReasonCodeOldNew;
import ca.bc.gov.educ.api.pen.replication.model.PenTwinTransaction;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public final class PenTwinTransactionValidator {
  private PenTwinTransactionValidator() {

  }

  public static Optional<String> validatePenTwinTransaction(final PenTwinTransaction penTwinTransaction) {
    if (StringUtils.isBlank(penTwinTransaction.getPenTwin1()) || StringUtils.isBlank(penTwinTransaction.getPenTwin2())) {
      return Optional.of("pen twin1 or pen twin2 is null or blank for transaction id " + penTwinTransaction.getTransactionID());
    }
    val matchReasonCodeOldNew = Arrays.stream(MatchReasonCodeOldNew.values()).filter(value -> value.getOldCode().equals(StringUtils.trim(penTwinTransaction.getTwinReason()))).findFirst();
    if (matchReasonCodeOldNew.isEmpty()) {
      return Optional.of("pen twin reason  " + penTwinTransaction.getTwinReason() + " could not be accepted for transaction id " + penTwinTransaction.getTransactionID());
    }
    return Optional.empty();
  }
}
