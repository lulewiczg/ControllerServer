package com.github.lulewiczg.controller.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.github.lulewiczg.controller.actions.KeyPressAction;
import com.github.lulewiczg.controller.actions.KeyReleaseAction;
import com.github.lulewiczg.controller.actions.LoginAction;
import com.github.lulewiczg.controller.common.Response;

public class Client {

    public static void main(String... args) throws IOException, InterruptedException, ClassNotFoundException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 5555));
        while (!socket.isConnected()) {
            Thread.sleep(1000);
        }
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new LoginAction("1234", "test", "lololol"));

        long nanoTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            KeyPressAction obj = new KeyPressAction('a');
            out.writeObject(obj);
            KeyReleaseAction obj2 = new KeyReleaseAction('a');
            out.writeObject(obj2);
            out.flush();
        }
        System.out.println((System.currentTimeMillis() - nanoTime));
        // TextAction obj = new TextAction("dupa hahah 123");
        // out.writeObject(obj);
        // out.flush();
        // System.out.println("ok");

        Response status = (Response) in.readObject();
        System.out.println(status);
        Thread.sleep(200);
        socket.close();
    }
}
