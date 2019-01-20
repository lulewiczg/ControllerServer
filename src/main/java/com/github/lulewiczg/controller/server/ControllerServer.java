package com.github.lulewiczg.controller.server;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.DisconnectException;
import com.github.lulewiczg.controller.exception.HandledException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.exception.ServerExitException;

/**
 * Server implementation.
 *
 * @author Grzegurz
 */
public class ControllerServer {

    private static final Logger log = LogManager.getLogger();
    private static final int ERROR_THRESHOLD = 5;
    private ServerState status = ServerState.SHUTDOWN;
    private static ControllerServer instance;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Thread thread;
    private ServerSocket server;
    private int errorCount;
    private Socket socket;
    private Settings config;
    private Object lock = new Object();

    private ControllerServer() {
        // Do nothing
    }

    /**
     * Gets server instance.
     *
     * @return instance
     */
    public static synchronized ControllerServer getInstance() {
        if (instance == null) {
            instance = new ControllerServer();
        }
        return instance;
    }

    /**
     * Handle server error.
     */
    private void onError() {
        stop();
        if (config.isRestartOnError()) {
            start(config);
        }
        throw new ServerExitException();
    }

    /**
     * Starts server on given port.
     *
     * @param port
     *            port
     * @param password
     *            password
     * @param testMode
     *            test mode
     */
    public void start(Settings config) {
        this.config = config;
        if (config.isTestMode()) {
            Action.setTestMode();
        } else {
            Action.setNormalMode();
        }
        thread = new Thread(() -> {
            try {
                setupSocket();
            } catch (IOException e) {
                log.error("Failed to setup socket");
                log.catching(Level.DEBUG, e);
                onError();
            }
            try {
                listen();
            } catch (HandledException | ServerExitException e) {
                // Do nothing, exception handled
            } catch (Exception e) {
                log.error(e.getMessage());
                log.catching(Level.DEBUG, e);
                onError();
            }
        });
        synchronized (lock) {
            thread.start();
        }
    }

    /**
     * Stops server.
     */
    public void stop() {
        synchronized (lock) {
            if (server != null && !server.isClosed()) {
                close(server);
                close(input);
                close(output);
                close(socket);
            }
            setStatus(ServerState.SHUTDOWN);
            log.info("Server stopped");
        }
    }

    /**
     * Sets up socket.
     *
     * @throws IOException
     *             the IOException
     */
    private void setupSocket() throws IOException {
        server = new ServerSocket(config.getPort());
        setStatus(ServerState.WAITING);
        log.info(String.format("Waiting for connection on port %s...", config.getPort()));
        socket = server.accept();
        socket.setReuseAddress(false);
        socket.setKeepAlive(true);
    }

    /**
     * Closes resources.
     *
     * @param c
     *            resource
     */
    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.catching(Level.DEBUG, e);
            }
        }
    }

    /**
     * Restart server.
     */
    public void restart() {
        stop();
        start(config);
    }

    /**
     * Listens for connection.
     *
     * @throws Exception
     *             the Exception
     */
    private void listen() throws Exception {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        while (!socket.isConnected() && inputStream.available() == 0) {
            Thread.sleep(1000);
        }
        log.info("Trying to connect...");
        input = new ObjectInputStream(inputStream);
        output = new ObjectOutputStream(outputStream);
        Response res = null;
        errorCount = 0;
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            try {
                res = handleAction(input.readObject());
                sendResponse(output, res);
            } catch (Exception e) {
                res = handleException(e);
            }
            errorCount = 0;
        }
    }

    /**
     * Handles exception
     *
     * @param e
     * @throws Exception
     *             the Exception
     */
    private Response handleException(Exception e) throws Exception {
        log.catching(Level.DEBUG, e);
        Status status = Status.NOT_OK;
        errorCount++;
        boolean handled = false;
        if (e instanceof SocketException || e instanceof EOFException) {
            isError();
            setStatus(ServerState.CONNECTION_ERROR);
            handled = true;
        } else if (e instanceof DisconnectException) {
            log.info("Disconnected");
            sendResponse(output, new Response(Status.OK));
            onError();
        } else if (e instanceof SocketException) {
            if (getStatus() != ServerState.SHUTDOWN) {
                log.info("Socket error");
                isError();
            }
            handled = true;
        } else if (e instanceof LoginException) {
            LoginException le = (LoginException) e;
            log.info(String.format("User %s from %s tried to login with invalid password", le.getWho(), le.getWhere()));
            handled = true;
            status = Status.INVALID_PASSWORD;
        } else if (e instanceof ActionException) {
            handled = true;
        }
        log.error(e.getMessage());
        sendResponse(output, new Response(status, e));
        if (handled) {
            throw new HandledException(e);
        } else {
            throw e;
        }
    }

    /**
     * Handlers client's action
     *
     * @param action
     *            action
     * @return action result
     * @throws ActionException
     *             the ActionException
     */
    private Response handleAction(Object action) throws ActionException {
        log.debug(action);
        return ((Action) action).run(this);
    }

    /**
     * Checks if error threshold was exceeded.
     *
     * @return false if not exceeded
     */
    private boolean isError() {
        if (errorCount <= ERROR_THRESHOLD) {
            return false;
        }
        throw new RuntimeException("Connection lost");
    }

    /**
     * Sends response to client.
     *
     * @param output
     *            output
     * @param res
     *            response
     */
    private void sendResponse(ObjectOutputStream output, Response res) {
        boolean error = false;
        while (!error && !isError()) {
            try {
                output.writeObject(res);
                output.flush();
                return;
            } catch (IOException e) {
                log.catching(Level.DEBUG, e);
                error = true;
                errorCount++;
            }
        }
    }

    public synchronized ServerState getStatus() {
        return status;
    }

    public synchronized void setStatus(ServerState state) {
        if (status == state) {
            return;
        }
        log.debug(String.format("Status changed from %s to %s.", status, state));
        this.status = state;
    }

    public String getPassword() {
        return config.getPassword();
    }

}
