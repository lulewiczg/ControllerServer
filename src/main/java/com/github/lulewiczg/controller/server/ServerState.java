package com.github.lulewiczg.controller.server;

/**
 * States for server.
 *
 * @author Grzegurz
 */
public enum ServerState {
    CONNECTED("Connected"), WAITING("Wating"), SHUTDOWN("Shutdown"), CONNECTION_ERROR("Connection error");

    private String msg;

    private ServerState(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}
