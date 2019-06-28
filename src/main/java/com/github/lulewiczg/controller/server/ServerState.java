package com.github.lulewiczg.controller.server;

/**
 * States for server.
 *
 * @author Grzegurz
 */
public enum ServerState {
    CONNECTED("Connected", true), WAITING("Wating", true), SHUTDOWN("Shutdown", false),
    FORCED_SHUTDOWN("Shutdown", false);

    private String msg;

    private boolean running;

    private ServerState(String msg, boolean running) {
        this.msg = msg;
        this.running = running;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isRunning() {
        return running;
    }

}
