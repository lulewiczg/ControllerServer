package com.github.lulewiczg.controller.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.exception.SemaphoreException;

/**
 * Server implementation.
 *
 * @author Grzegurz
 */
@Service
public class ControllerServer {

    private static final Logger log = LogManager.getLogger(ControllerServer.class);
    private ServerState status = ServerState.SHUTDOWN;
    private ServerSocket server;
    private Socket socket;
    private ExecutorService exec;
    private Semaphore semaphore = new Semaphore(1, true);
    private Semaphore listenerSemaphore = new Semaphore(1, true);
    private volatile InternalServerState internalState = InternalServerState.DOWN;

    private SettingsComponent config;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ApplicationContext context;

    // context.getBean
    private ActionProcessor processor;

    @Autowired
    public ControllerServer(SettingsComponent config) {
        this.config = config;
        if (config.getSettings() != null && config.getSettings().isAutostart()) {
            start();
        }
    }

    /**
     * Handles server logic.
     */
    private void doServer() {
        acquire(listenerSemaphore);
        try {
            setupSocket();
            doActions();
        } catch (Exception e) {
            exceptionService.error(log, e);
            softStop();
        } finally {
            release(listenerSemaphore);
        }
    }

    /**
     * Starts server.
     */
    public void start() {
        acquire(semaphore);
        exec = Executors.newSingleThreadExecutor();
        exec.submit(this::doServer);
        internalState = InternalServerState.UP;
        release(semaphore);
    }

    /**
     * Performs soft stop. Server will restart.
     */
    private void softStop() {
        acquire(semaphore);
        setStatus(ServerState.SHUTDOWN);
        if (server != null && !server.isClosed()) {
            Common.close(server);
            Common.close(processor);
            Common.close(socket);
        }
        if (internalState != InternalServerState.DOWN_AND_DONT_START) {
            internalState = InternalServerState.DOWN;
        }
        log.info("Server stopped");
        release(semaphore);
    }

    /**
     * Forces server to stop. Will not restart.
     */
    public void stop() {
        internalState = InternalServerState.DOWN_AND_DONT_START;
        exec.shutdownNow();
        softStop();
    }

    /**
     * Sets up socket.
     *
     * @throws IOException
     *             when socket could not be set up
     */
    private void setupSocket() throws IOException {
        server = new ServerSocket(config.getSettings().getPort());
        setStatus(ServerState.WAITING);
        log.info(String.format("Waiting for connection on port %s...", config.getSettings().getPort()));
        socket = server.accept();
        socket.setReuseAddress(false);
        socket.setKeepAlive(true);
    }

    /**
     * Listens for incoming connections.
     *
     * @throws Exception
     *             the Exception
     */
    private void doActions() throws Exception {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        while (!socket.isConnected() && inputStream.available() == 0) {
            Thread.sleep(500);
        }
        log.info("Trying to connect...");
        ClientConnection clientConnection = context.getBean(ClientConnection.class, inputStream, outputStream);
        processor = context.getBean(ActionProcessor.class, clientConnection);
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            processor.processAction(this);
        }
    }

    /**
     * Changes server state to connected.
     */
    public void login() {
        setStatus(ServerState.CONNECTED);
    }

    /**
     * Disconnects client.
     */
    public void logout() {
        softStop();
        log.info("Disconnected");
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
            throw new SemaphoreException(e);
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

    public ServerState getStatus() {
        return status;
    }

    public InternalServerState getInternalState() {
        return internalState;
    }

    public void setInternalState(InternalServerState internalState) {
        this.internalState = internalState;
    }

}
