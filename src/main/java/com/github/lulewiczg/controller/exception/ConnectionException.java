package com.github.lulewiczg.controller.exception;

/**
 * Exception for client connection errors.
 *
 * @author Grzegurz
 */
public class ConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectionException(Exception e) {
        super(e);
    }
}
