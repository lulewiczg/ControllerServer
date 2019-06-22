package com.github.lulewiczg.controller.exception;

/**
 * Exception for situations when semaphore can not be acquired.
 *
 * @author Grzegurz
 */
public class SemaphoreException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SemaphoreException(Exception e) {
        super(e);
    }

}
