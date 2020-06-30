package com.github.lulewiczg.controller.common;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.io.Closeable;
import java.io.IOException;

/**
 * Common utility class.
 *
 * @author Grzegurz
 */
@Log4j2
@UtilityClass
public class Common {

    /**
     * Closes resources.
     *
     * @param c resource
     */
    public void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.catching(Level.DEBUG, e);
            }
        }
    }

}
