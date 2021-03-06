package com.github.lulewiczg.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.connection.JsonClientConnection;
import com.github.lulewiczg.controller.common.Response;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * JSON client implementation.
 *
 * @author Grzegurz
 */
@Lazy
@Component
@Scope("prototype")
public class JsonClient implements Client {

    private final Socket socket;
    private final OutputStream out;
    private final InputStream in;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Scanner sc;


    @SneakyThrows
    public JsonClient(int port) {
        socket = new Socket();
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress("localhost", port));
        while (!socket.isConnected()) {
            Thread.sleep(1000);
        }
        out = socket.getOutputStream();
        in = socket.getInputStream();
        sc = new Scanner(in);
        sc.useDelimiter(JsonClientConnection.DELIM);
    }

    @Override
    @SneakyThrows
    public Response doAction(Action action) {
        out.write((mapper.writeValueAsString(action) + JsonClientConnection.DELIM).getBytes());
        out.flush();
        return mapper.readValue(sc.next(), Response.class);
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
