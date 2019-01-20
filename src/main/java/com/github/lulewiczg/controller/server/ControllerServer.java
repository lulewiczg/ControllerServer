package com.github.lulewiczg.controller.server;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.LoginAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.DisconnectException;
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
    private boolean restartOnError = false;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Thread thread;
    private ServerSocket server;
    private int errorCount;
    private Socket socket;
    private int port;

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
        _stop();
        if (restartOnError) {
            try {
                _start();
            } catch (IOException e) {
                log.error("Error during error handling.");
                log.catching(Level.DEBUG, e);
            }
        }
    }

    /**
     * Starts server on given port.
     *
     * @param port
     *            port
     */
    public void start(int port) {
        this.port = port;
        thread = new Thread(() -> {
            try {
                _start();
                listen();
            } catch (InterruptedException e) {
                log.catching(Level.DEBUG, e);
            } catch (DisconnectException e) {
                log.info("Disconnected");
                log.catching(Level.DEBUG, e);
                onError();
            } catch (LoginException e) {
                StringBuilder str = new StringBuilder();
                str.append("User ").append(e.getWho()).append(", from ").append(e.getWhere())
                        .append(" tried to login with invalid password!");
                log.info(str.toString());
                log.catching(Level.DEBUG, e);
                onError();
            } catch (SocketException e) {
                if (getStatus() == ServerState.SHUTDOWN) {
                    log.catching(Level.TRACE, e);
                } else {
                    log.info("Socket error");
                    log.catching(Level.DEBUG, e);
                    onError();
                }
            } catch (Exception e) {
                log.catching(Level.DEBUG, e);
                onError();
            }
        });
        thread.start();
    }

    /**
     * Stops server.
     */
    public void stop() {
        _stop();
    }

    /**
     * Starts server.
     *
     * @throws IOException
     *             the IOException
     */
    private void _start() throws IOException {
        server = new ServerSocket(port);
        setStatus(ServerState.WAITING);
        log.info("Waiting for connection on port " + port + "...");
        socket = server.accept();
        socket.setReuseAddress(false);
        socket.setKeepAlive(true);
    }

    /**
     * Stops server.
     */
    private void _stop() {
        if (!server.isClosed()) {
            close(server);
            close(input);
            close(output);
            close(socket);
        }
        setStatus(ServerState.SHUTDOWN);
        log.info("Server stopped");
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
        start(port);
    }

    /**
     * Listens for connection/
     *
     * @throws InterruptedException
     *             the InterruptedException
     * @throws IOException
     *             the IOException
     */
    private void listen() throws InterruptedException, IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        while (!socket.isConnected() && inputStream.available() == 0) {
            Thread.sleep(1000);
        }
        log.info("Trying to connect...");
        input = new ObjectInputStream(inputStream);
        output = new ObjectOutputStream(outputStream);
        estabilish();
        Response res = null;
        errorCount = 0;
        setStatus(ServerState.CONNECTED);
        while (!socket.isClosed() && getStatus() == ServerState.CONNECTED) {
            try {
                Action a = (Action) input.readObject();
                log.debug(a);
                a.doAction();
                res = new Response(Status.OK);
            } catch (SocketException | EOFException e) {
                Thread.sleep(1000);
                errorCount++;
                log.catching(Level.DEBUG, e);
                isError();
                setStatus(ServerState.CONNECTION_ERROR);
                continue;
            } catch (DisconnectException e) {
                sendResponse(output, new Response(Status.OK));
                throw e;
            } catch (Exception e) {
                log.catching(e);
                res = new Response(Status.NOT_OK, e);
            }
            sendResponse(output, res);
            errorCount = 0;
        }
    }

    /**
     * Tries to connect with client.
     *
     * @throws IOException
     *             the IOException
     */
    private void estabilish() throws IOException {
        LoginAction login;
        try {
            login = (LoginAction) input.readObject();
            login.setServerPassword(Settings.getSettings().getPassword());
            login.doAction();
            Response r = new Response(Status.SERVER_OK);
            output.writeObject(r);
        } catch (ClassNotFoundException e) {
            log.catching(Level.DEBUG, e);
            throw new ConnectException();
        } catch (LoginException e) {
            Response r = new Response(Status.INVALID_PASSWORD);
            output.writeObject(r);
            throw e;
        }
        log.info("Connected: " + login.getInfo() + ", " + login.getIp());
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

    public void setPort(int port) {
        this.port = port;
    }

    private synchronized void setStatus(ServerState state) {
        if (status == state) {
            return;
        }
        log.debug(String.format("Status changed from %s to %s.", status, state));
        this.status = state;
    }

    public void setRestartOnError(boolean restartOnError) {
        this.restartOnError = restartOnError;
    }
}
