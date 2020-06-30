package com.github.lulewiczg.controller.actions.processor;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.exception.ServerExitException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;

/**
 * Class for processing client actions.
 *
 * @author Grzegurz
 */
@Lazy
@Service
@Scope("prototype")
@Log4j2
public class ActionProcessor implements Closeable {

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
     * @param server server
     * @throws Exception the Exception
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
            handleException(e);
        }
    }

    /**
     * Handles exceptions.
     *
     * @param e exception to handle
     * @throws Exception the Exception
     */
    private void handleException(Exception e) throws Exception {
        Status status = Status.NOT_OK;
        if (e instanceof LoginException) {
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
        } else if (e instanceof ServerExitException) {
            throw e;
        } else {
            exceptionService.error(log, "Unexpected exception", e);
            sendResponse(new Response(status, e));
            throw e;
        }
        sendResponse(new Response(status, e));
    }

    /**
     * Sends response to client.
     *
     * @param res response
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
