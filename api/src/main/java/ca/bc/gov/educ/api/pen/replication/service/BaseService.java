package ca.bc.gov.educ.api.pen.replication.service;

import org.apache.commons.lang3.StringUtils;

public abstract class BaseService implements EventService {

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
}
