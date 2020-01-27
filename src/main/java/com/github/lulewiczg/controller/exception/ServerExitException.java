package com.github.lulewiczg.controller.exception;

/**
 * Exception to shutdown server.
 *
 * @author Grzegurz
 */
public class ServerExitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServerExitException() {
        super("Client requested server to stop");
    }

}
