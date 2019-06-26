package com.github.lulewiczg.controller.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.actions.impl.KeyPressAction;
import com.github.lulewiczg.controller.actions.impl.KeyReleaseAction;
import com.github.lulewiczg.controller.actions.impl.MouseButtonPressAction;
import com.github.lulewiczg.controller.actions.impl.ServerStopAction;
import com.github.lulewiczg.controller.actions.processor.connection.ObjectStreamClientConnection;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { UIConfiguration.class, TestConfiguration.class, ObjectStreamClientConnection.class })
@EnableAutoConfiguration
public class ControllerServerTest {
    private static final int PORT = 5555;
    private static final String PASSWORD = "1234";

    private Client client;
    private Client client2;

    @Autowired
    private ControllerServerManager serverRunner;

    @Autowired
    private ControllerServer server;

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private Robot robot;

    @MockBean
    private ServerWindow window;

    /**
     * Stops server after test.
     *
     * @throws InterruptedException
     *             the InterruptedException
     * @throws IOException
     */
    @AfterEach
    public void after() throws InterruptedException, IOException {
        if (serverRunner.isRunning()) {
            serverRunner.stop();
        }
        waitForState(ServerState.SHUTDOWN);
        if (client != null) {
            client.close();
        }
        if (client2 != null) {
            client2.close();
        }
        client = null;
        client2 = null;
    }

    @Test
    @DisplayName("Server not restart after logout")
    public void testStateAfterLogout() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();

        Mockito.verify(server).login();
        Mockito.verify(server).logout();

        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Server is stopped after stop")
    public void testServerStateAfterStop() throws Exception {
        startServer();
        server.stop();
        waitForState(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server can't be started twice")
    public void test() throws Exception {
        startServer();
        waitForState(ServerState.WAITING);
        assertThrows(ServerAlreadyRunningException.class, () -> startServer());
    }

    @Test
    @DisplayName("Server losts connection to client")
    public void testServerConnectionLost() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Connect to server in down state")
    public void testConnectToDownServer() throws Exception {
        assertThrows(ConnectException.class, () -> new Client(PORT));
        waitForState(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Connect to server in up state")
    public void testLoginToUpServer() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.login(PASSWORD);
        assertOK(response);
    }

    @Test
    @DisplayName("Connect to server wiith invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.login("4321");

        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    public void testLoginWhenLoggedIn() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.login(PASSWORD);

        assertError(response, AlreadyLoggedInException.class);
        waitForState(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.logout();

        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Relog")
    public void testRelogin() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.login(PASSWORD);
        assertOK(response);
        Response response2 = client.logout();
        assertEquals(Status.OK, response2.getStatus());
        waitForState(ServerState.WAITING);
        client2 = new Client(PORT);
        Response response3 = client2.login(PASSWORD);

        assertOK(response3);
    }

    @Test
    @DisplayName("Connects to server using invalid port")
    public void testConnectToInvalidPort() throws Exception {
        startServer();
        assertThrows(ConnectException.class, () -> client = new Client(4321));
    }

    @Test
    @DisplayName("Sends action without login")
    public void testSendActionWithoutLogin() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Sends action after logout")
    public void testSendActionAfterLogout() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();
        waitForState(ServerState.WAITING);
        client2 = new Client(PORT);
        Response response = client2.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Sends action")
    public void testSendAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
        assertOK(response);
    }

    @Test
    @DisplayName("Send multiple actions")
    public void testSendMultipleActions() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        Instant then = Instant.now();
        for (int i = 0; i < 10000; i++) {
            Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
            assertOK(response);
            Response response2 = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON2_DOWN_MASK));
            assertOK(response2);
        }
        System.out.println(
                "--------> Time needed for 20000 actions: " + Duration.between(then, Instant.now()).getNano() / 1000000.0);
    }

    @Test
    @DisplayName("Two clients connect")
    public void testConnectTwoClients() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        assertThrows(AssertionFailedError.class, () -> assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(PORT);
            client2.login(PASSWORD);
        }));
        Mockito.verify(server, Mockito.times(1)).login();
    }

    @Test
    @DisplayName("Server does not restart after stop action")
    public void testStopAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.doAction(new ServerStopAction());
        assertOK(response);
        waitForState(ServerState.SHUTDOWN);
        Thread.sleep(1000);
        waitForState(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Reconnect after connection lost")
    public void testReconnectAfterConnectionLost() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(PORT);
            Response login = client2.login(PASSWORD);
            assertEquals(Status.OK, login.getStatus());
        });

        Mockito.verify(server, Mockito.times(2)).login();
    }

    @Test
    @DisplayName("Actions are executed in order")
    public void testActionsInOrde() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        List<Response> responses = new ArrayList<>();
        responses.add(client.doAction(new KeyPressAction(1)));
        responses.add(client.doAction(new KeyPressAction(2)));
        responses.add(client.doAction(new KeyReleaseAction(1)));
        responses.add(client.doAction(new KeyReleaseAction(2)));

        responses.forEach(i -> assertOK(i));
        InOrder inOrder = Mockito.inOrder(robot);
        inOrder.verify(robot).keyPress(1);
        inOrder.verify(robot).keyPress(2);
        inOrder.verify(robot).keyRelease(1);
        inOrder.verify(robot).keyRelease(2);
    }

    @Test
    @DisplayName("State in UI is updated")
    public void testUpdateStateInUI() throws Exception {
        startServer();
        waitForState(ServerState.WAITING);
        Mockito.verify(window).updateUI(ServerState.WAITING);
    }

    /**
     * Starts server with default settings.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */

    private void startServer() throws InterruptedException {
        Mockito.when(settings.isAutostart()).thenReturn(true);
        Mockito.when(settings.getPassword()).thenReturn(PASSWORD);
        Mockito.when(settings.getPort()).thenReturn(PORT);

        serverRunner.start();
        waitForState(ServerState.WAITING);
    }

    /**
     * Waits for server to change state.
     *
     * @param state
     *            target state
     * @throws InterruptedException
     */
    private void waitForState(ServerState state) throws InterruptedException {
        for (int i = 0; i < 10 && serverRunner.getStatus() != state; i++) {
            Thread.sleep(100);
        }
        assertEquals(state, serverRunner.getStatus(), "Invalid server state");
    }

    /**
     * Checks if response was invalid.
     *
     * @param response
     *            response
     * @param e
     *            expected exception
     */
    private void assertError(Response response, Class<? extends Exception> e) {
        assertEquals(Status.NOT_OK, response.getStatus());
        assertNotNull(response.getException());
        assertEquals(e, response.getException().getClass());
    }

    /**
     * Checks if response and server state are OK.
     *
     * @param response
     *            server response
     */
    private void assertOK(Response response) {
        assertEquals(Status.OK, response.getStatus());
        assertEquals(ServerState.CONNECTED, server.getStatus());
    }

}
