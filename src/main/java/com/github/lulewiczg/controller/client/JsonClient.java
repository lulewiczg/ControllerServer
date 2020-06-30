package com.github.lulewiczg.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.DisconnectAction;
import com.github.lulewiczg.controller.actions.impl.LoginAction;
import com.github.lulewiczg.controller.actions.impl.ServerStopAction;
import com.github.lulewiczg.controller.actions.processor.connection.JSONClientConnection;
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

    public JsonClient(int port) throws IOException, InterruptedException {
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
        out.write((mapper.writeValueAsString(action) + JSONClientConnection.DELIM).getBytes());
        out.flush();
        return mapper.readValue(sc.next(), Response.class);
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
        JsonClient c = new JsonClient(55552);
        try (c) {
            c.login("FajneHasloTakieNieZaLatweXD123");
            // c.doAction(new TextAction("abc"));
            c.doAction(new ServerStopAction());
            Thread.sleep(1000);
        }
    }
}
