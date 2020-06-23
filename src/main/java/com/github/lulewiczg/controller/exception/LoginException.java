package com.github.lulewiczg.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exception for login error event.
 *
 * @author Grzegurz
 */
@Getter
@AllArgsConstructor
public class LoginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String who;
    private final String where;

}
