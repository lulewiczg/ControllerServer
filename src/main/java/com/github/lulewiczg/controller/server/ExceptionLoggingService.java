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
     * Logs exception with ERROR level.
     *
     * @param log logger
     * @param e   exception
     */
    public void error(Logger log, Exception e) {
        log.error(e.getMessage());
        log.catching(Level.DEBUG, e);
    }

    /**
     * Logs exception with ERROR level.
     *
     * @param log logger
     * @param msg message
     * @param e   exception
     */
    public void error(Logger log, String msg, Exception e) {
        log.error(msg);
        log.catching(Level.DEBUG, e);
    }

    /**
     * Logs exception with INFO level.
     *
     * @param log logger
     * @param msg message
     * @param e   exception
     */
    public void info(Logger log, String msg, Exception e) {
        log.info(msg);
        log.catching(Level.DEBUG, e);
    }

    /**
     * Logs exception with INFO level.
     *
     * @param log logger
     * @param e   exception
     */
    public void info(Logger log, Exception e) {
        log.info(e.getMessage());
        log.catching(Level.DEBUG, e);
    }

    /**
     * Logs exception with DEBUG level.
     *
     * @param log logger
     * @param e   exception
     */
    public void debug(Logger log, Exception e) {
        log.catching(Level.DEBUG, e);
    }

}
