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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.exception.SemaphoreException;
import com.github.lulewiczg.controller.ui.ServerWindow;

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
    private Socket socket;
    private ExecutorService exec;
    private Semaphore semaphore = new Semaphore(1, true);
    private Semaphore listenerSemaphore = new Semaphore(1, true);

    Thread serverThread = new Thread();// dummy thread

    private ServerSocket server;

    @Autowired
    private SettingsComponent config;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ApplicationContext context;

    private ServerWindow window;

    // context.getBean
    private ActionProcessor processor;

    /**
     * Handles server logic.
     */
    private void doServer() {
        acquire(listenerSemaphore);
        try {
            setupSocket();
            doActions();
            serverThread.interrupt();
        } catch (BeanCreationException e) {
            exceptionService.error(log, "Address is already in use", e);
            stop();
        } catch (Exception e) {
            exceptionService.error(log, e);
            serverThread.interrupt();
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
        serverThread = context.getBean(ControllerServerThread.class, new Thread(this::doServer));// TODO skip new
        exec.submit(serverThread);
        log.info("Server started");
        release(semaphore);
    }

    /**
     * Forces server to stop. Will not restart.
     */
    public void stop() {
        setStatus(ServerState.FORCED_SHUTDOWN);
        serverThread.interrupt();
        exec.shutdownNow();
    }

    /**
     * Sets up socket.
     *
     * @throws IOException
     *             when socket could not be set up
     */
    private void setupSocket() throws IOException {
        server = context.getBean(ServerSocket.class);
        setStatus(ServerState.WAITING);
        log.info("Waiting for connection on port {}...", config.getPort());
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
     * Closes resources used by server and changes status.
     */
    void closeServer() {
        acquire(semaphore);
        if (server != null && !server.isClosed()) {
            Common.close(server);
            Common.close(processor);
            Common.close(socket);
        }
        if (status != ServerState.FORCED_SHUTDOWN) {
            setStatus(ServerState.SHUTDOWN);
        }
        release(semaphore);
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
        serverThread.interrupt();
        log.info("Disconnected");
    }

    /**
     * Updates UI.
     */
    private void updateUI() {
        if (window != null) {
            window.updateUI(status);
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
            Thread.currentThread().interrupt();
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
            log.debug("Status changed from {} to {}.", status, state);
        }
        this.status = state;
        updateUI();
    }

    public ServerState getStatus() {
        return status;
    }

    @Autowired(required = false)
    public void setWindow(ServerWindow window) {
        this.window = window;
        window.updateUI(status);
    }

    @Bean
    @Scope(value = "prototype")
    private ControllerServerThread getThread(Runnable lambda) {
        return new ControllerServerThread(lambda);
    }

    /**
     * Class for handling server thread, handling non-interruptible socket.
     *
     * @author Grzegorz
     */
    public class ControllerServerThread extends Thread {

        private Runnable lambda;

        private ControllerServerThread(Runnable lambda) {
            this.lambda = lambda;
        }

        /**
         * Runs lambda expression.
         */
        @Override
        public void run() {
            this.lambda.run();
        }

        /**
         * Fixes Socket blocking read, which ignores interruptions.
         */
        @Override
        public void interrupt() {
            super.interrupt();
            closeServer();
            log.info("Server stopped");
        }

    }
}
