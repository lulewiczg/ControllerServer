package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.actions.processor.connection.ObjectStreamClientConnection;
import com.github.lulewiczg.controller.ui.ServerWindow;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
class ControllerServerTest {

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

    @MockBean
    private TimeoutWatcher watcher;

    @Mock
    private Socket socket;

    @Mock
    private InputStream input;

    @Mock
    private OutputStream out;

    private final Executor exec = Executors.newCachedThreadPool();

    @BeforeEach
    void before() throws Exception {
        when(socket.getInputStream()).thenReturn(input);
        when(socket.getOutputStream()).thenReturn(out);

        when(socketServer.accept()).thenReturn(socket);

        when(settings.getConnectionType()).thenReturn(ObjectStreamClientConnection.NAME);
    }

    @AfterEach
    void after() {
        if (server.getStatus().isRunning()) {
            server.stop();
        }
        server.setStatus(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server waits for connect")
    void testConnectNothing() throws Exception {
        when(socket.isConnected()).thenReturn(false);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(true);

        startAndWait();

        verify(server).closeServer();
        verify(processor, never()).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Server waits for connection forever")
    void testConnectWait() throws Exception {
        when(socket.isConnected()).thenReturn(false);

        startAndWait();

        assertThat(server.getStatus(), Matchers.equalTo(ServerState.WAITING));
    }

    @Test
    @DisplayName("Server waits for input")
    void testConnectWaitForInput() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(0);
        when(socket.isClosed()).thenReturn(true);

        startAndWait();

        verify(server).closeServer();
        verify(processor, never()).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform 1 action")
    void testPerformSingleAction() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false, true);

        startAndWait();

        verify(server).closeServer();
        verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform 1 action that stops server")
    void testPerformSingleActionStateChange() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false);
        when(server.getStatus()).thenReturn(ServerState.CONNECTED, ServerState.SHUTDOWN);

        startAndWait();

        verify(server).closeServer();
        verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Connect to server and perform multiple actions")
    void testPerformMultipleAction() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false, false, false, true);

        startAndWait();

        verify(server).closeServer();
        verify(processor, times(3)).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("Server does not start when port is already used")
    void testPortAlreadyUsed() throws Exception {
        when(socketServer.accept()).thenThrow(BeanCreationException.class);
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false);

        startAndWait();

        verify(server).stop();
        verify(socket, never()).getInputStream();
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.FORCED_SHUTDOWN));
    }

    @Test
    @DisplayName("Action throws exception")
    void testActionException() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false);
        doThrow(Exception.class).when(processor).processAction(server);

        startAndWait();

        verify(server).closeServer();
        verify(processor).processAction(server);
        assertThat(server.getStatus(), Matchers.equalTo(ServerState.SHUTDOWN));
    }

    @Test
    @DisplayName("User login")
    void testLogin() {
        server.login();

        verify(server).setStatus(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("User logout")
    void testLogout() {
        server.logout();
        verify(server).closeServer();
        verify(server).setStatus(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Call for timeout watcher")
    void testTimeout() throws Exception {
        when(socket.isConnected()).thenReturn(true);
        when(input.available()).thenReturn(1);
        when(socket.isClosed()).thenReturn(false);
        long time = System.currentTimeMillis();

        exec.execute(server::start);

        verify(watcher, timeout(1000)).watch(any());// Mockito bug
        verify(server, atLeast(1)).updateLastTime();
        assertThat(server.getLastAcionTime(), Matchers.greaterThan(time));
    }

    /**
     * Starts server and waits for start.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */
    private void startAndWait() throws InterruptedException {
        exec.execute(server::start);
        Thread.sleep(100);
    }

}
