package ca.bc.gov.educ.api.pen.replication.struct;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  /**
   * Possible match added event outcome.
   */
  POSSIBLE_MATCH_ADDED,
  /**
   * Possible match deleted event outcome.
   */
  POSSIBLE_MATCH_DELETED,
  /**
   * Student created event outcome.
   */
  STUDENT_CREATED,
  /**
   * Student updated event outcome.
   */
  STUDENT_UPDATED,
  /**
   * Merge created event outcome.
   */
  MERGE_CREATED,
  /**
   * Merge deleted event outcome.
   */
  MERGE_DELETED,
  STUDENT_NOT_FOUND,
  STUDENTS_FOUND
}
