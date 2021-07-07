package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.PenDemogTransaction;
import ca.bc.gov.educ.api.pen.replication.struct.StudentCreate;
import ca.bc.gov.educ.api.pen.replication.struct.StudentUpdate;

/**
 * The type Student decorator.
 */
public abstract class StudentDecorator implements StudentMapper {
  private final StudentMapper delegate;

  /**
   * Instantiates a new Student decorator.
   *
   * @param delegate the delegate
   */
  protected StudentDecorator(final StudentMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public StudentCreate toStudentCreate(final PenDemogTransaction penDemogTransaction) {
    final var entity = this.delegate.toStudentCreate(penDemogTransaction);
    entity.setDob(this.getFormattedDOB(penDemogTransaction.getBirthDate()));
    return entity;
  }

  @Override
  public StudentUpdate toStudent(final PenDemogTransaction penDemogTransaction) {
    final var entity = this.delegate.toStudent(penDemogTransaction);
    entity.setDob(this.getFormattedDOB(penDemogTransaction.getBirthDate()));
    return entity;
  }

  private String getFormattedDOB(final String birthDate) {
    if (birthDate.length() == 8) {
      return birthDate.substring(0, 4).concat("-").concat(birthDate.substring(4, 6)).concat("-").concat(birthDate.substring(6, 8));
    }
    return birthDate;
  }
}
