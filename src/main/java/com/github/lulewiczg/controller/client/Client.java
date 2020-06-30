package com.github.lulewiczg.controller.client;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;

import java.io.Closeable;

/**
 * Client to conenct with server.
 *
 * @author Grzegurz
 */
public interface Client extends Closeable {

    /**
     * Logs in to the server.
     *
     * @param password password
     * @return server response
     */
    Response login(String password);

    /**
     * Disconnects from server.
     *
     * @return server response
     */
    Response logout();

    /**
     * Executes server action.
     *
     * @param action action
     * @return server response
     */
    Response doAction(Action action);

}
