package com.github.lulewiczg.controller.actions.processor;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.DisconnectException;
import com.github.lulewiczg.controller.exception.HandledException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;

/**
 * Interface for serializing actions.
 *
 * @author Grzegurz
 */
public abstract class ActionProcessor implements Closeable {
    protected static final Logger log = LogManager.getLogger();
    protected int errorCount;

    @Autowired
    private ControllingService controllingService;

    /**
     * Writes response to output stream.
     */
    protected abstract void write(Response r) throws IOException;

    /**
     * Reads next action.
     *
     * @return action action
     * @throws Exception
     *             the Exception
     */
    protected abstract Action getNext() throws Exception;

    /**
     * Processes action
     *
     * @param server
     *            server
     * @throws Exception
     *
     */
    public void processAction(ControllerServer server) throws Exception {
        Action action = getNext();
        log.debug(action);
        try {
            Response res = action.run(server, controllingService);
            sendResponse(res);
        } catch (Exception e) {
            handleException(server, e);
        }
        errorCount = 0;
    }

    /**
     * Handles exception
     *
     * @param e
     *            exception to handle
     * @throws Exception
     *             the Exception
     */
    private void handleException(ControllerServer server, Exception e) throws Exception {
        log.catching(Level.DEBUG, e);
        Status status = Status.NOT_OK;
        errorCount++;
        boolean handled = false;
        boolean logException = true;
        if (e instanceof SocketException || e instanceof EOFException) {
            handled = true;
            log.error("Connection lost");
            logException = false;
            server.setStatus(ServerState.SHUTDOWN);
        } else if (e instanceof DisconnectException) {
            handled = true;
            log.info("Disconnected");
            status = Status.OK;
            server.setStatus(ServerState.SHUTDOWN);
        } else if (e instanceof LoginException) {
            handled = true;
            LoginException le = (LoginException) e;
            log.info(String.format("User %s from %s tried to login with invalid password", le.getWho(), le.getWhere()));
            status = Status.INVALID_PASSWORD;
        } else if (e instanceof ActionException) {
            handled = true;
        }
        if (logException) {
            log.error(e.getMessage());
        }
        sendResponse(new Response(status, e));
        if (handled) {
            throw new HandledException(e);
        } else {
            throw e;
        }
    }

    /**
     * Sends response to client.
     *
     * @param output
     *            output
     * @param res
     *            response
     */
    private void sendResponse(Response res) {
        boolean error = false;
        while (!error) {
            try {
                write(res);
                return;
            } catch (IOException e) {
                log.catching(Level.DEBUG, e);
                error = true;
                errorCount++;
            }
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void errInc() {
        errorCount++;
    }

}
