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
  GET_STUDENTS
}
