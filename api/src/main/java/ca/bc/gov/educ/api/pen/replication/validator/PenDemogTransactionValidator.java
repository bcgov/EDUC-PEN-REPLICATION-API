package ca.bc.gov.educ.api.pen.replication.validator;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PenDemogTransactionValidator {

  private static final List<String> studentStatus = Arrays.asList("A", "M", "D");
  private static final List<String> demogCodes = Arrays.asList("A", "C", "F");
  private static final List<String> sexCodes = Arrays.asList("F", "M", "X", "U");

  private PenDemogTransactionValidator() {

  }

  public static Optional<String> validatePenDemogForCreate(final PenDemogTransaction penDemogTransaction) {
    if (StringUtils.isBlank(penDemogTransaction.getBirthDate())) {
      return Optional.of("BirthDate is null or blank for transaction id " + penDemogTransaction.getTransactionID());
    } else if (!validateDOB(penDemogTransaction.getBirthDate())) {
      return Optional.of("BirthDate is invalid " + penDemogTransaction.getBirthDate() + " for transaction id " + penDemogTransaction.getTransactionID());
    } else if (StringUtils.isBlank(penDemogTransaction.getStatus())) {
      return Optional.of("Student status is null or blank for transaction id " + penDemogTransaction.getTransactionID());
    } else if (!studentStatus.contains(StringUtils.trim(penDemogTransaction.getStatus()))) {
      return Optional.of("Student status " + penDemogTransaction.getStatus() + " is not accepted. for transaction id " + penDemogTransaction.getTransactionID());
    } else if (StringUtils.isBlank(penDemogTransaction.getDemogCode())) {
      return Optional.of("Student demog code is null or blank for transaction id " + penDemogTransaction.getTransactionID());
    } else if (!demogCodes.contains(StringUtils.trim(penDemogTransaction.getDemogCode()))) {
      return Optional.of("Student demog code " + penDemogTransaction.getDemogCode() + " is not accepted. for transaction id " + penDemogTransaction.getTransactionID());
    } else if (StringUtils.isBlank(penDemogTransaction.getSex())) {
      return Optional.of("Student sex code null or blank for transaction id " + penDemogTransaction.getTransactionID());
    } else if (!sexCodes.contains(StringUtils.trim(penDemogTransaction.getSex()))) {
      return Optional.of("Student sex code " + penDemogTransaction.getSex() + " is not accepted. for transaction id " + penDemogTransaction.getTransactionID());
    } else if (StringUtils.isBlank(penDemogTransaction.getSurname())) {
      return Optional.of("Student legal surname is null or blank for transaction id " + penDemogTransaction.getTransactionID());
    } else {
      return Optional.empty();
    }
  }

  public static Optional<String> validatePenDemogForUpdate(final PenDemogTransaction penDemogTransaction) {
    if (StringUtils.isBlank(penDemogTransaction.getPen())) {
      return Optional.of("Student pen is null or blank for transaction id " + penDemogTransaction.getTransactionID());
    }
    return validatePenDemogForCreate(penDemogTransaction);
  }

  public static boolean validateDOB(final String birthDate) {
    final String dob = birthDate.substring(0, 4).concat("-").concat(birthDate.substring(4, 6)).concat("-").concat(birthDate.substring(6, 8));
    try {
      final LocalDate dobDate = LocalDate.parse(dob);
      if (dobDate.isAfter(LocalDate.now())) {
        return false;
      }
    } catch (final Exception e) {
      return false;
    }
    return true;
  }
}
