
package com.github.lulewiczg.controller.client;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.DisconnectAction;
import com.github.lulewiczg.controller.actions.impl.LoginAction;
import com.github.lulewiczg.controller.actions.impl.TextAction;
import com.github.lulewiczg.controller.common.Response;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Object stream client implementation.
 *
 * @author Grzegurz
 */
@Lazy
@Component
@Scope("prototype")
public class ObjectStreamClient implements Client {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ObjectStreamClient(int port) throws IOException, InterruptedException {
        socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", port));
        while (!socket.isConnected()) {
            Thread.sleep(1000);
        }
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    @SneakyThrows
    public Response login(String password) {
        Response res = doAction(new LoginAction(password, "Client", "localhost"));
        Thread.sleep(100);
        return res;
    }

    @Override
    @SneakyThrows
    public Response logout() {
        return doAction(new DisconnectAction());
    }

    @Override
    @SneakyThrows
    public Response doAction(Action action) {
        out.writeObject(action);
        return (Response) in.readObject();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    /**
     * Example connection to server.
     *
     * @param args args
     * @throws Exception the Exception
     */
    public static void main(String[] args) throws Exception {
        Client c = new ObjectStreamClient(5555);
        try (c) {
            c.login("1234");
            c.doAction(new TextAction("abc"));
            Thread.sleep(1000);
        }
    }
}
