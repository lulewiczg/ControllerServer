package com.github.lulewiczg.controller.exception;

/**
 * Exception for invalid action result.
 *
 * @author Grzegurz
 */
public class ActionException extends Exception {
    private static final long serialVersionUID = 1L;

    public ActionException(String msg) {
        super(msg);
    }
}
