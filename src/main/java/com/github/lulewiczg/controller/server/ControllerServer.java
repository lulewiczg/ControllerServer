package com.github.lulewiczg.controller.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.EmptyActionProcessor;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.exception.HandledException;

/**
 * Server implementation.
 *
 * @author Grzegurz
 */
public class ControllerServer {

    private static final Logger log = LogManager.getLogger();
    private ServerState status = ServerState.SHUTDOWN;
    private static ControllerServer instance;
    private ServerSocket server;
    private ActionProcessor processor;
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
        processor = new EmptyActionProcessor();
        try {
            setupConnection();
            doActions();
        } finally {
            processor = new EmptyActionProcessor();
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
    private void stopServer() {
        acquire(semaphore);
        setStatus(ServerState.SHUTDOWN);
        if (server != null && !server.isClosed()) {
            Common.close(server);
            Common.close(processor);
            Common.close(socket);
        }
        log.info("Server stopped");
        release(semaphore);
    }

    /**
     * Forces server to stop.
     */
    public void stop() {
        processor.setErrorCount(Common.ERROR_THRESHOLD + 1);
        stopServer();
    }

    /**
     * Handle fatal server error that can not be recovered.
     */
    private void onFatalError() {
        processor.errInc();
        stopServer();
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
            Thread.sleep(500);
        }
        log.info("Trying to connect...");
        processor = config.getSerialier().getSerializer(inputStream, outputStream);
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            processor.processAction(this);
        }
    }

    /**
     * Checks if error threshold was exceeded.
     *
     * @return false if shouldn't
     */
    private boolean shouldFail() {
        if (config.isRestartOnError() && processor.getErrorCount() <= Common.ERROR_THRESHOLD) {
            return false;
        }
        throw new RuntimeException("Connection lost");
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
