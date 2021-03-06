package com.github.lulewiczg.controller.exception;

/**
 * Exception for actions invoked when not logged in.
 *
 * @author Grzegurz
 */
public class AlreadyLoggedInException extends ActionException {
    private static final long serialVersionUID = 1L;

    public AlreadyLoggedInException(String msg) {
        super(msg);
    }
}
