package fi.csc.emrex.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpentika on 17/11/15.
 */
public class PersonalLogger {
    final static Logger logger = LoggerFactory.getLogger(PersonalLogger.class);

    public static void log(String something){
        logger.info(something);
    }
}
