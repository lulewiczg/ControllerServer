package com.github.lulewiczg.controller.client;

import com.github.lulewiczg.controller.actions.impl.ServerStopAction;

/**
 * Client connection example.
 */
public class ClientExample {

    /**
     * Example connection to server.
     *
     * @param args args
     * @throws Exception the Exception
     */
    public static void main(String[] args) throws Exception {
        JsonClient c = new JsonClient(55552);
        try (c) {
            c.login("password");
            // c.doAction(new TextAction("abc"));
            c.doAction(new ServerStopAction());
            Thread.sleep(1000);
        }
    }
}
