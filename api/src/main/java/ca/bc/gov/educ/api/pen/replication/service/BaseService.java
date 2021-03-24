package ca.bc.gov.educ.api.pen.replication.service;

import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.BaseRequest;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseService<T extends BaseRequest> implements EventService<T> {

  protected final RestUtils restUtils;

  protected BaseService(final RestUtils restUtils) {
    this.restUtils = restUtils;
  }

  protected String formatDateTime(String activityDate) {
    if (StringUtils.isBlank(activityDate)) {
      return activityDate;
    }
    activityDate = activityDate.replace("T", " ");
    if (activityDate.length() > 19) {
      activityDate = activityDate.substring(0, 19);
    }
    return activityDate;
  }

  /**
   * Gets student true pen number.
   *
   * @param trueStudentID the true student id
   * @return the student true number
   */
  protected String getStudentPen(final String trueStudentID) {
    return this.restUtils.getStudentPen(trueStudentID).orElseThrow();
  }

}
