package com.github.lulewiczg.controller.common;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common utility class.
 *
 * @author Grzegurz
 */
public class Common {
    private static final Logger log = LogManager.getLogger();
    public static final int ERROR_THRESHOLD = 5;

    /**
     * Closes resources.
     *
     * @param c
     *            resource
     */
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.catching(Level.DEBUG, e);
            }
        }
    }
}
