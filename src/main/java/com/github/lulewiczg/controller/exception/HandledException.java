package com.github.lulewiczg.controller.exception;

/**
 * Exception that was handled successfully.
 *
 * @author Grzegurz
 */
public class HandledException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HandledException(Exception e) {
        super(e);
    }
}
