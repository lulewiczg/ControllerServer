package com.github.lulewiczg.controller.exception;

/**
 * Exception for login error event.
 *
 * @author Grzegurz
 */
public class LoginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String who;
    private final String where;

    public String getWho() {
        return who;
    }

    public String getWhere() {
        return where;
    }

    public LoginException(String who, String where) {
        this.who = who;
        this.where = where;
    }

}
