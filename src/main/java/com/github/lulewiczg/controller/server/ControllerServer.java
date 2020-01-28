package com.github.lulewiczg.controller.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Common;
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
    private ServerSocket server;
    private Object startLock = new Object();
    private Object stopLock = new Object();

    @Autowired
    private SettingsComponent config;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ApplicationContext context;

    private ServerWindow window;

    // context.getBean
    private ActionProcessor processor;

    private Thread serverThread = new Thread();// dummy thread

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

}
