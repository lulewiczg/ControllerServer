package com.github.lulewiczg.controller.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.DisconnectAction;
import com.github.lulewiczg.controller.actions.impl.LoginAction;
import com.github.lulewiczg.controller.actions.impl.ServerStopAction;
import com.github.lulewiczg.controller.actions.processor.connection.JSONClientConnection;
import com.github.lulewiczg.controller.common.Response;

/**
 * Client to conenct with server.
 *
 * @author Grzegurz
 */
public class Client implements Closeable {

    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private ObjectMapper mapper = new ObjectMapper();
    private Scanner sc;

    public Client(int port) throws IOException, InterruptedException {
        socket = new Socket();
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress("localhost", port));
        while (!socket.isConnected()) {
            Thread.sleep(1000);
        }
        out = socket.getOutputStream();
        in = socket.getInputStream();
        sc = new Scanner(in);
        sc.useDelimiter(JSONClientConnection.DELIM);
    }

    /**
     * Logs in to the server.
     *
     * @param password
     *            password
     * @return server response
     * @throws IOException
     *             IOException
     * @throws ClassNotFoundException
     *             the ClassNotFoundException
     * @throws InterruptedException
     *             the InterruptedException
     */
    public Response login(String password) throws IOException, ClassNotFoundException, InterruptedException {
        Response res = doAction(new LoginAction(password, "Client", "localhost"));
        Thread.sleep(100);
        return res;
    }

    /**
     * Disconnects from server.
     *
     * @return server response
     * @throws IOException
     *             IOException
     * @throws ClassNotFoundException
     *             the ClassNotFoundException
     */
    public Response logout() throws IOException, ClassNotFoundException {
        return doAction(new DisconnectAction());
    }

    /**
     * Executes server action.
     *
     * @param action
     *            action
     * @return server response
     * @throws IOException
     *             the IOException
     * @throws ClassNotFoundException
     *             the ClassNotFoundException
     */
    public Response doAction(Action action) throws IOException, ClassNotFoundException {
        out.write((mapper.writeValueAsString(action) + JSONClientConnection.DELIM).getBytes());
        out.flush();
        return mapper.readValue(sc.next(), Response.class);
    }

    /**
     * Closes connection.
     *
     * @throws IOException
     *             the IOException
     */
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    /**
     * Example connection to server.
     *
     * @param args
     *            args
     * @throws Exception
     *             the Exception
     */
    public static void main(String[] args) throws Exception {
        Client c = new Client(55552);
        try (c) {
            c.login("FajneHasloTakieNieZaLatweXD123");
            // c.doAction(new TextAction("abc"));
            c.doAction(new ServerStopAction());
            Thread.sleep(1000);
        }
    }
}
