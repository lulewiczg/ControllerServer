package com.github.lulewiczg.controller.actions.processor;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;

/**
 * Interface for serializing actions.
 *
 * @author Grzegurz
 */
@Lazy
@Service
@Scope("prototype")
public class ActionProcessor implements Closeable {
    protected static final Logger log = LogManager.getLogger();

    @Autowired
    private ControllingService controllingService;

    @Autowired
    private ExceptionLoggingService exceptionService;

    private ClientConnection connection;

    @Autowired
    public ActionProcessor(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * Processes action
     *
     * @param server
     *            server
     * @throws Exception
     *
     */
    public void processAction(ControllerServer server) throws Exception {
        Action action = connection.getNext();
        log.debug(action);
        try {
            Response res = action.run(server, controllingService);
            sendResponse(res);
            if (res.getCallback() != null) {
                res.getCallback().accept(server);
            }
        } catch (Exception e) {
            handleException(server, e);
        }
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
        Status status = Status.NOT_OK;
        boolean handled = true;
        if (e instanceof SocketException || e instanceof EOFException) {
            exceptionService.error(log, "Connection lost", e);
            server.setStatus(ServerState.SHUTDOWN);
        } else if (e instanceof LoginException) {
            LoginException le = (LoginException) e;
            exceptionService.error(log,
                    String.format("User %s from %s tried to login with invalid password", le.getWho(), le.getWhere()), e);
            status = Status.INVALID_PASSWORD;
        } else if (e instanceof AlreadyLoggedInException) {
            exceptionService.error(log, "Already logged in", e);
            status = Status.NOT_OK;
        } else if (e instanceof AuthorizationException) {
            exceptionService.error(log, "Permission denied", e);
            status = Status.NOT_OK;
        } else {
            exceptionService.error(log, e);
            handled = false;
        }
        sendResponse(new Response(status, e));
        if (!handled) {
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
                connection.write(res);
                return;
            } catch (IOException e) {
                exceptionService.error(log, e);
                error = true;
            }
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
