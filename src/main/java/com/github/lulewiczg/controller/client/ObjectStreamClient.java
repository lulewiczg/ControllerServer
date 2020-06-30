
package com.github.lulewiczg.controller.client;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;
import lombok.SneakyThrows;

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
public class ObjectStreamClient implements Client {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    @SneakyThrows
    public ObjectStreamClient(int port) {
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
}

