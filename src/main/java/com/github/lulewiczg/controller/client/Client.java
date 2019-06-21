package com.github.lulewiczg.controller.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.DisconnectAction;
import com.github.lulewiczg.controller.actions.impl.LoginAction;
import com.github.lulewiczg.controller.actions.impl.TextAction;
import com.github.lulewiczg.controller.common.Response;

/**
 * Client to conenct with server.
 *
 * @author Grzegurz
 */
public class Client implements Closeable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(int port) throws IOException, InterruptedException {
        socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", port));
        while (!socket.isConnected()) {
            Thread.sleep(1000);
        }
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
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
        out.writeObject(action);
        return (Response) in.readObject();
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
        Client c = new Client(5555);
        try (c) {
            c.login("1234");
            c.doAction(new TextAction("abc"));
            Thread.sleep(1000);
        }
    }
}
