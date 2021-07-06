package ca.bc.gov.educ.api.pen.replication.constants;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  /**
   * Initiate success event outcome.
   */
  INITIATE_SUCCESS,
  /**
   * Saga completed event outcome.
   */
  SAGA_COMPLETED,
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
  /**
   * Students not found event outcome.
   */
  STUDENTS_NOT_FOUND,
  /**
   * Students found event outcome.
   */
  STUDENTS_FOUND,
  /**
   * Next pen number retrieved event outcome.
   */
  NEXT_PEN_NUMBER_RETRIEVED,
  /**
   * Student already exist event outcome.
   */
  STUDENT_ALREADY_EXIST,
  /**
   * Pen demog added event outcome.
   */
  PEN_DEMOG_ADDED,
  /**
   * Pen demog transaction updated event outcome.
   */
  PEN_DEMOG_TRANSACTION_UPDATED,
  /**
   * Pen demog updated event outcome.
   */
  PEN_DEMOG_UPDATED,
  /**
   * Student found event outcome.
   */
  STUDENT_FOUND,
  /**
   * Student not found event outcome.
   */
  STUDENT_NOT_FOUND,

  PEN_TWINS_CREATED,
  PEN_TWIN_TRANSACTION_UPDATED,
  PEN_TWINS_DELETED

}
