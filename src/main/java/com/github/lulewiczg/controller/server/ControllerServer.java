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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.common.Common;

/**
 * Server implementation.
 *
 * @author Grzegurz
 */
@Lazy
@Service
public class ControllerServer {

    private static final Logger log = LogManager.getLogger(ControllerServer.class);
    private ServerState status = ServerState.SHUTDOWN;
    private ServerSocket server;
    private Socket socket;
    private ExecutorService exec;
    private Semaphore semaphore = new Semaphore(1, true);
    private Semaphore listenerSemaphore = new Semaphore(1, true);

    private SettingsComponent config;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ApplicationContext context;

    // context.getBean
    private ActionProcessor processor;

    @Autowired
    @Qualifier("emptyProcessor")
    private ActionProcessor emptyProcessor;

    @Autowired
    public ControllerServer(SettingsComponent config) {
        this.config = config;
        if (config.getSettings() != null && config.getSettings().isAutostart()) {
            start();
        }
    }

    /**
     * Listens for connections.
     */
    private void doServer() {
        acquire(listenerSemaphore);
        processor = emptyProcessor;
        try {
            setupConnection();
            doActions();
        } finally {
            processor = emptyProcessor;
            release(listenerSemaphore);
        }
    }

    /*
     * Listens for actions and executes them.
     */
    private void doActions() {
        try {
            listen();
        } catch (Exception e) {
            exceptionService.log(log, e);
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
            exceptionService.log(log, "Failed to setup socket", e);
            onFatalError();
        }
    }

    /**
     * Starts server.
     *
     */
    public void start() {
        acquire(semaphore);
        exec = Executors.newSingleThreadExecutor();
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
        stopServer();
        exec.shutdownNow();
    }

    /**
     * Handle fatal server error that can not be recovered.
     */
    private void onFatalError() {
        stopServer();
        restartIfNeeded();
    }

    /**
     * Restarts server after stop when required.
     */
    private void restartIfNeeded() {
        if (config.getSettings().isRestartOnError() && getStatus() == ServerState.SHUTDOWN) {
            start();
        } else {
            throw new RuntimeException("Connection lost");
        }
    }

    /**
     * Sets up socket.
     *
     * @throws IOException
     *             the IOException
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
     * Changes server state to connected.
     */
    public void login() {
        setStatus(ServerState.CONNECTED);
    }

    /**
     * Disconnects client.
     */
    public void logout() {
        stop();
        log.info("Disconnected");
        restartIfNeeded();
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
        processor = context.getBean(ActionProcessor.class, inputStream, outputStream);
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            processor.processAction(this);
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

    public void setStatus(ServerState state) {
        if (status != state) {
            log.debug(String.format("Status changed from %s to %s.", status, state));
        }
        this.status = state;
    }

    public ServerState getStatus() {
        return status;
    }

}
