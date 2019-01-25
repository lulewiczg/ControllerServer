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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
    private ServerSocket server;
    private int errorCount;
    private Socket socket;
    private Settings config;
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Semaphore semaphore = new Semaphore(1, true);
    private Semaphore listenerSemaphore = new Semaphore(1, true);

    private ControllerServer() {
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
     * Listens for connections.
     */
    private void doServer() {
        acquire(listenerSemaphore);
        try {
            setupConnection();
            doActions();
        } finally {
            release(listenerSemaphore);
        }
    }

    /*
     * Listens for actions and executes them.
     */
    private void doActions() {
        try {
            listen();
        } catch (HandledException e) {
            log.catching(Level.TRACE, e);
            onFatalError();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.catching(Level.DEBUG, e);
            onFatalError();
        }
    }

    /**
     * Sets up connection with client.
     */
    private void setupConnection() {
        try {
            setupSocket();
        } catch (IOException e) {
            log.error("Failed to setup socket");
            log.catching(Level.DEBUG, e);
            onFatalError();
        }
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
        acquire(semaphore);
        this.config = config;
        if (config.isTestMode()) {
            Action.setTestMode();
        } else {
            Action.setNormalMode();
        }
        exec.submit(this::doServer);
        release(semaphore);
    }

    /**
     * Stops server.
     */
    public void stop() {
        acquire(semaphore);
        if (status != ServerState.CONNECTION_ERROR) {
            setStatus(ServerState.SHUTDOWN);
        }
        if (server != null && !server.isClosed()) {
            close(server);
            close(input);
            close(output);
            close(socket);
        }
        log.info("Server stopped");
        release(semaphore);
    }

    /**
     * Handle fatal server error that can not be recovered.
     */
    private void onFatalError() {
        errorCount++;
        stop();
        shouldFail();
        if (config.isRestartOnError() && getStatus() != ServerState.CONNECTED) {
            start(config);
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
        errorCount = 0;
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            try {
                Response res = handleAction(input.readObject());
                sendResponse(output, res);
            } catch (Exception e) {
                handleException(e);
            }
            errorCount = 0;
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
    private void handleException(Exception e) throws Exception {
        log.catching(Level.DEBUG, e);
        Status status = Status.NOT_OK;
        errorCount++;
        boolean handled = false;
        boolean logException = true;
        if (e instanceof SocketException || e instanceof EOFException) {
            setStatus(ServerState.CONNECTION_ERROR);
            handled = true;
            log.error("Connection lost");
            logException = false;
        } else if (e instanceof DisconnectException) {
            log.info("Disconnected");
            status = Status.OK;
            setStatus(ServerState.SHUTDOWN);
        } else if (e instanceof LoginException) {
            LoginException le = (LoginException) e;
            log.info(String.format("User %s from %s tried to login with invalid password", le.getWho(), le.getWhere()));
            handled = true;
            status = Status.INVALID_PASSWORD;
        } else if (e instanceof ActionException) {
            handled = true;
        }
        if (logException) {
            log.error(e.getMessage());
        }
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
     * @return false if shouldn't
     */
    private boolean shouldFail() {
        if (config.isRestartOnError() && errorCount <= ERROR_THRESHOLD) {
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
        while (!error) {
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

    /**
     * Acquires lock.
     *
     * @param semaphore
     *            semaphore to lock
     */
    private void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Releases lock.
     *
     * @param semaphore
     *            to release
     */
    private void release(Semaphore semaphore) {
        semaphore.release();
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

    public void setStatus(ServerState state) {
        if (status != state) {
            log.debug(String.format("Status changed from %s to %s.", status, state));
        }
        this.status = state;
    }

    public String getPassword() {
        return config.getPassword();
    }

    public ServerState getStatus() {
        return status;
    }

}
