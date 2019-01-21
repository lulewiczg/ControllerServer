package com.github.lulewiczg.controller.exception;

/**
 * Exception for login when already logged in.
 *
 * @author Grzegurz
 */
public class AuthorizationException extends ActionException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String msg) {
        super(msg);
    }
}
