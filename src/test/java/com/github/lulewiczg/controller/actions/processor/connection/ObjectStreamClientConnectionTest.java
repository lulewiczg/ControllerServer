package com.github.lulewiczg.controller.actions.processor.connection;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.MouseMoveAction;
import com.github.lulewiczg.controller.actions.impl.PingAction;
import com.github.lulewiczg.controller.actions.impl.TextAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

class ObjectStreamClientConnectionTest {


    @Test
    @DisplayName("Streams are closed")
    void testClose() throws IOException {
        WatcherOutputStream out = new WatcherOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new PingAction());
        WatcherInputStream in = new WatcherInputStream(baos.toByteArray());
        ObjectStreamClientConnection connection = new ObjectStreamClientConnection(in, out);

        connection.close();

        assertThat(in.closed, is(true));
        assertThat(out.closed, is(true));

    }

    @Test
    @DisplayName("Read action")
    void testReadAction() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(new TextAction("abc"));
        ObjectStreamClientConnection connection = new ObjectStreamClientConnection(new ByteArrayInputStream(baos.toByteArray()), new ByteArrayOutputStream());

        Action action = connection.getNext();

        assertThat(action, is(new TextAction("abc")));
    }

    @Test
    @DisplayName("Read multiple actions")
    void testReadActions() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(new TextAction("abc"));
        out.writeObject(new TextAction("123"));
        out.writeObject(new MouseMoveAction(20, 30));

        ObjectStreamClientConnection connection = new ObjectStreamClientConnection(new ByteArrayInputStream(baos.toByteArray()), new ByteArrayOutputStream());

        Action action = connection.getNext();
        Action action2 = connection.getNext();
        Action action3 = connection.getNext();

        assertThat(action, is(new TextAction("abc")));
        assertThat(action2, is(new TextAction("123")));
        assertThat(action3, is(new MouseMoveAction(20, 30)));
    }


    @Test
    @DisplayName("Write response")
    void testWriteResponse() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new PingAction());

        ObjectStreamClientConnection connection = new ObjectStreamClientConnection(new ByteArrayInputStream(baos.toByteArray()), out);
        Response response = new Response(Status.NOT_OK, new AuthorizationException("aaa"));

        connection.write(response);

        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        Response read = (Response) objectInputStream.readObject();
        assertThat(read, is(response));
    }

    private static class WatcherOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static class WatcherInputStream extends ByteArrayInputStream {
        private boolean closed;

        public WatcherInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}