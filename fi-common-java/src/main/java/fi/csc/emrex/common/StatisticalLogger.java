package fi.csc.emrex.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpentika on 17/11/15.
 */
public class StatisticalLogger {
    final static Logger logger = LoggerFactory.getLogger(StatisticalLogger.class);

    public static void log(String something){
        logger.info(something);
    }
}
