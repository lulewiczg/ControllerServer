package com.github.lulewiczg.controller.client;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.DisconnectAction;
import com.github.lulewiczg.controller.actions.impl.LoginAction;
import com.github.lulewiczg.controller.common.Response;
import lombok.SneakyThrows;

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
    @SneakyThrows
    default Response login(String password) {
        Response res = doAction(new LoginAction(password, "Client", "localhost"));
        Thread.sleep(100);
        return res;
    }

    /**
     * Disconnects from server.
     *
     * @return server response
     */
    @SneakyThrows
    default Response logout() {
        return doAction(new DisconnectAction());
    }

    /**
     * Executes server action.
     *
     * @param action action
     * @return server response
     */
    Response doAction(Action action);

}
