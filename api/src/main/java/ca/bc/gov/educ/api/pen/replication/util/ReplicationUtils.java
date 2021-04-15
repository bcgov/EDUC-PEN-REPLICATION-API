package ca.bc.gov.educ.api.pen.replication.util;

import org.apache.commons.lang3.StringUtils;

public class ReplicationUtils {

    public static String getBlankWhenNull(final String s){
        if(StringUtils.isNotEmpty(s)){
            return s;
        }
        //Return a blank to PEN_DEMOG in these cases as per our reqs
        return " ";
    }
}
