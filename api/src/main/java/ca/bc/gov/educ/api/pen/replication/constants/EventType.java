package ca.bc.gov.educ.api.pen.replication.constants;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Mark saga complete event type.
   */
  MARK_SAGA_COMPLETE,
  /**
   * Initiated event type.
   */
  INITIATED,
  /**
   * Create student event type.
   */
  CREATE_STUDENT,
  /**
   * Update student event type.
   */
  UPDATE_STUDENT,
  /**
   * Add possible match event type.
   */
  ADD_POSSIBLE_MATCH,
  /**
   * Delete possible match event type.
   */
  DELETE_POSSIBLE_MATCH,
  /**
   * Create Merge Data.
   */
  CREATE_MERGE,

  /**
   * Delete Merge Data.
   */
  DELETE_MERGE,
  /**
   * Get students event type.
   */
  GET_STUDENTS,
  /**
   * Get next pen number event type.
   */
  GET_NEXT_PEN_NUMBER,
  /**
   * Add pen demog event type.
   */
  ADD_PEN_DEMOG,
  /**
   * Update pen demog transaction event type.
   */
  UPDATE_PEN_DEMOG_TRANSACTION,
  /**
   * Update pen demog event type.
   */
  UPDATE_PEN_DEMOG,
  /**
   * Get student event type.
   */
  GET_STUDENT,
  /**
   * Add student event type.
   */
  ADD_STUDENT,
  CREATE_PEN_TWINS,
  UPDATE_PEN_TWIN_TRANSACTION,
  DELETE_PEN_TWINS
}
