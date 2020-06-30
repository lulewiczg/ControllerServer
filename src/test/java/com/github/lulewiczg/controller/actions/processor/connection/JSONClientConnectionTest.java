package com.github.lulewiczg.controller.actions.processor.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.impl.MouseMoveAction;
import com.github.lulewiczg.controller.actions.impl.TextAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

class JSONClientConnectionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Streams are closed")
    void testClose() throws IOException {
        InputStream in = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);
        JSONClientConnection connection = new JSONClientConnection(in, out);

        connection.close();

        verify(in, atLeastOnce()).close();
        verify(out).close();
    }

    @Test
    @DisplayName("Read action")
    void testReadAction() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(mapper.writeValueAsBytes(new TextAction("abc")));
        JSONClientConnection connection = new JSONClientConnection(in, new ByteArrayOutputStream());

        Action action = connection.getNext();

        assertThat(action, is(new TextAction("abc")));
    }

    @Test
    @DisplayName("Read multiple actions")
    void testReadActions() throws Exception {
        String data = mapper.writeValueAsString(new TextAction("abc")) + "<>" + mapper.writeValueAsString(new TextAction("123")) + "<>" +
                mapper.writeValueAsString(new MouseMoveAction(20, 30));
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
        JSONClientConnection connection = new JSONClientConnection(in, new ByteArrayOutputStream());

        Action action = connection.getNext();
        Action action2 = connection.getNext();
        Action action3 = connection.getNext();

        assertThat(action, is(new TextAction("abc")));
        assertThat(action2, is(new TextAction("123")));
        assertThat(action3, is(new MouseMoveAction(20, 30)));
    }

    @Test
    @DisplayName("Read action with delimiter")
    void testReadActionDelimiter() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(mapper.writeValueAsBytes(new TextAction("a<//>b")));
        JSONClientConnection connection = new JSONClientConnection(in, new ByteArrayOutputStream());

        Action action = connection.getNext();

        assertThat(action, is(new TextAction("a<>b")));
    }

    @Test
    @DisplayName("Write response")
    void testWriteResponse() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSONClientConnection connection = new JSONClientConnection(in, out);
        Response response = new Response(Status.NOT_OK, new AuthorizationException("aaa"));

        connection.write(response);

        String responseStr = new String(out.toByteArray());
        assertThat(mapper.readValue(responseStr.replace("<>", ""), Response.class), is(response));
    }
}