package com.github.lulewiczg.controller.server;

import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.actions.processor.connection.ObjectStreamClientConnection;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockRequiredUIConfiguration.class, ObjectStreamClientConnection.class, ControllerServer.class,
        ExceptionLoggingService.class })
@EnableAutoConfiguration
public class ControllerServerTest {

    @SpyBean
    private ControllerServer server;

    @MockBean
    private SettingsComponent settings;

    @MockBean
    private ServerWindow window;

    @MockBean
    private ServerSocket socketServer;

    @MockBean
    private ActionProcessor processor;

    @MockBean
    private ClientConnection connection;

    @Mock
    private Socket socket;

    @Mock
    private InputStream input;

    @Mock
    private OutputStream out;

    @BeforeEach
    public void before() throws Exception {
        Mockito.when(socket.getInputStream()).thenReturn(input);
        Mockito.when(socket.getOutputStream()).thenReturn(out);

        Mockito.when(socketServer.accept()).thenReturn(socket);
    }

    @AfterEach
    public void after() {
        if (server.getStatus().isRunning()) {
            server.stop();
        }
        server.setStatus(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server waits for connect")
    public void testConnectNothing() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(false);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(true);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor, Mockito.never()).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Server waits for connection forever")
    public void testConnectWait() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(false);

        startAndWait(true);

        assertThat(server.getStatus(), Matchers.equalTo(ServerState.WAITING));
    }

    @Test
    @DisplayName("Server waits for input")
    public void testConnectWaitForInput() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(0);
        Mockito.when(socket.isClosed()).thenReturn(true);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor, Mockito.never()).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform 1 action")
    public void testPerformSingleAction() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(false, true);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform 1 action that stops server")
    public void testPerformSingleActionStateChange() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(false);
        Mockito.when(server.getStatus()).thenReturn(ServerState.CONNECTED, ServerState.SHUTDOWN);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform multiple actions")
    public void testPerformMultipleAction() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(false, false, false, true);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor, Mockito.times(3)).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Server does not start when port is already used")
    public void testPortAlreadyUsed() throws Exception {
        Mockito.when(socketServer.accept()).thenThrow(BeanCreationException.class);
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(false);

        startAndWait(false);

        Mockito.verify(server).stop();
        Mockito.verify(socket, Mockito.never()).getInputStream();
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.FORCED_SHUTDOWN));
    }

    @Test
    @DisplayName("Action throws exception")
    public void testActionException() throws Exception {
        Mockito.when(socket.isConnected()).thenReturn(true);
        Mockito.when(input.available()).thenReturn(1);
        Mockito.when(socket.isClosed()).thenReturn(false);
        Mockito.doThrow(Exception.class).when(processor).processAction(server);

        startAndWait(true);

        Mockito.verify(server).softStop();
        Mockito.verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("User login")
    public void testLogin() throws Exception {
        server.login();

        Mockito.verify(server).setStatus(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("User logout")
    public void testLogout() throws Exception {
        server.logout();

        Mockito.verify(server, Mockito.atLeastOnce()).setStatus(ServerState.SHUTDOWN);
        Mockito.verify(server).softStop();
    }

    /**
     * Starts server and waits for start.
     *
     * @param started
     *            should start
     * @throws InterruptedException
     *             the InterruptedException
     */
    private void startAndWait(boolean started) throws InterruptedException {
        server.start();
        Thread.sleep(100);
    }

}
