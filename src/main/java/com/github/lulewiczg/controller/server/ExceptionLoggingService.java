package com.github.lulewiczg.controller.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Service for logging error messages and exceptions.
 *
 * @author Grzegurz
 */
@Service
public class ExceptionLoggingService {

    /**
     * Logs exception.
     *
     * @param log
     *            logger
     * @param e
     *            exception
     */
    public void log(Logger log, Exception e) {
        log.error(e.getMessage());
        log.catching(Level.DEBUG, e);
    }

    /**
     * Logs exception.
     *
     * @param log
     *            logger
     * @param msg
     *            message
     * @param e
     *            exception
     */
    public void log(Logger log, String msg, Exception e) {
        log.error(msg);
        log.catching(Level.DEBUG, e);
    }

}
