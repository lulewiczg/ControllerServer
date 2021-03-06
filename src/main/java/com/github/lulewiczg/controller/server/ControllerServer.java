package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.exception.ServerExitException;
import com.github.lulewiczg.controller.ui.ServerWindow;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server implementation.
 *
 * @author Grzegurz
 */
@Lazy
@Log4j2
@Service
public class ControllerServer {

    @Getter
    private ServerState status = ServerState.SHUTDOWN;
    private Socket socket;
    private ServerSocket server;
    private final Object startLock = new Object();
    private final Object stopLock = new Object();

    @Autowired
    private SettingsComponent config;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TimeoutWatcher watcher;

    private ServerWindow window;

    @Getter
    private Long lastAcionTime;

    // context.getBean
    private ActionProcessor processor;

    private Thread serverThread = new Thread();// dummy thread

    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void createTimeoutThread() {
        timeoutExecutor.scheduleAtFixedRate(() -> watcher.watch(this), 1L, 1L, TimeUnit.SECONDS);
    }

    /**
     * Starts server.
     */
    void start() {
        synchronized (startLock) {
            log.info("Server started");
            this.serverThread = Thread.currentThread();
            try {
                setupSocket();
                doActions();
                closeServer();
            } catch (BeanCreationException e) {
                exceptionService.error(log, "Address is already in use", e);
                stop();
            } catch (SocketException e) {
                exceptionService.debug(log, e);
                closeServer();
            } catch (ServerExitException e) {
                exceptionService.info(log, e);
                stop();
            } catch (Exception e) {
                exceptionService.error(log, e);
                closeServer();
            }
            log.info("Server stopped");
        }
    }

    /**
     * Disconnects client.
     */
    public void logout() {
        closeServer();
        log.info("Disconnected");
    }

    /**
     * Forces server to stop. Will not restart.
     */
    void stop() {
        setStatus(ServerState.FORCED_SHUTDOWN);
        closeServer();
    }

    /**
     * Sets up socket.
     *
     * @throws IOException when socket could not be set up
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
     * @throws Exception the Exception
     */
    private void doActions() throws Exception {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        while (!socket.isConnected() && inputStream.available() == 0) {
            Thread.sleep(500);
        }
        log.info("Trying to connect...");
        ClientConnection clientConnection = (ClientConnection) context.getBean(config.getConnectionType(), inputStream, outputStream);
        processor = context.getBean(ActionProcessor.class, clientConnection);
        while (!socket.isClosed() && getStatus() != ServerState.SHUTDOWN) {
            processor.processAction(this);
            updateLastTime();
        }
    }

    /**
     * Updates time when last action was invkoed.
     */
    public void updateLastTime() {
        lastAcionTime = System.currentTimeMillis();
        log.trace("Time update {}", lastAcionTime);
    }

    /**
     * Closes resources used by server and changes status.
     */
    void closeServer() {
        synchronized (stopLock) {
            if (server != null && !server.isClosed()) {
                Common.close(server);
                Common.close(processor);
                Common.close(socket);
            }
            if (status != ServerState.FORCED_SHUTDOWN) {
                setStatus(ServerState.SHUTDOWN);
            }
            serverThread.interrupt();
        }
    }

    /**
     * Changes server state to connected.
     */
    public void login() {
        setStatus(ServerState.CONNECTED);
    }

    /**
     * Updates UI if window is present.
     */
    private void updateUI() {
        if (window != null) {
            window.updateUI(status);
        }
    }

    void setStatus(ServerState state) {
        if (status != state) {
            log.debug("Status changed from {} to {}.", status, state);
        }
        this.status = state;
        updateUI();
    }

    @Autowired(required = false)
    public void setWindow(ServerWindow window) {
        this.window = window;
        window.updateUI(status);
    }

}
