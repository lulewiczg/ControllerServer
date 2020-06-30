package com.github.lulewiczg.controller.server;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * States for server.
 *
 * @author Grzegurz
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ServerState {
    CONNECTED("Connected", true),
    WAITING("Waiting", true),
    SHUTDOWN("Shutdown", false),
    FORCED_SHUTDOWN("Shutdown", false);

    private final String msg;

    private final boolean running;

}
