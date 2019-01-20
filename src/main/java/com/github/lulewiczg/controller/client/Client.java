package com.github.lulewiczg.controller.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.DisconnectAction;
import com.github.lulewiczg.controller.actions.LoginAction;
import com.github.lulewiczg.controller.common.Response;

/**
 * Client to conenct with server.
 *
 * @author Grzegurz
 */
public class Client {

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
     */
    public Response login(String password) throws IOException, ClassNotFoundException {
        return doAction(new LoginAction(password, "Client", "localhost"));
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

}
