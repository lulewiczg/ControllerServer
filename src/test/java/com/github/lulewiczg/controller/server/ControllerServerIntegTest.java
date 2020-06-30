package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.AWTSpringApplicationContextLoader;
import com.github.lulewiczg.controller.actions.impl.KeyPressAction;
import com.github.lulewiczg.controller.actions.impl.KeyReleaseAction;
import com.github.lulewiczg.controller.actions.impl.MouseButtonPressAction;
import com.github.lulewiczg.controller.actions.impl.ServerStopAction;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.client.JsonClient;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.ui.ServerWindow;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.awt.*;
import java.awt.event.InputEvent;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 */
@ContextConfiguration(loader = AWTSpringApplicationContextLoader.class)
abstract class ControllerServerIntegTest {

    private Client client;

    private Client client2;

    @Value("${com.github.lulewiczg.setting.password}")
    private String password;

    @Value("${com.github.lulewiczg.setting.port}")
    private int port;

    @Autowired
    private ControllerServerManager serverRunner;

    @SpyBean
    private ControllerServer server;

    @MockBean
    private Robot robot;

    @MockBean
    private ServerWindow window;

    @Autowired
    protected ApplicationContext context;

    /**
     * Gets client.
     *
     * @return client
     */
    protected abstract Client getClient(int port);

    /**
     * Stops server after test.
     *
     * @throws Exception the Exception
     */
    @AfterEach
    void after() throws Exception {
        if (serverRunner.isRunning()) {
            serverRunner.stop();
            waitForState(ServerState.FORCED_SHUTDOWN);
        }
        if (client != null) {
            client.close();
        }
        if (client2 != null) {
            client2.close();
        }
        client = null;
        client2 = null;
    }

    @BeforeEach
    void before() {
        server.setStatus(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server restart after logout")
    void testStateAfterLogout() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        client.logout();

        Mockito.verify(server).login();
        Mockito.verify(server).logout();

        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Server is stopped after stop")
    void testServerStateAfterStop() throws Exception {
        startServer();
        server.stop();
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server can't be started twice")
    void test() throws Exception {
        startServer();
        waitForState(ServerState.WAITING);
        assertThrows(ServerAlreadyRunningException.class, () -> startServer());
    }

    @Test
    @DisplayName("Server loses connection to client")
    void testServerConnectionLost() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        client.close();
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Connect to server in down state")
    void testConnectToDownServer() throws Exception {
        assertThrows(ConnectException.class, () -> new JsonClient(port));
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Connect to server in up state")
    void testLoginToUpServer() throws Exception {
        startServer();
        client = getClient(port);
        Response response = client.login(password);
        assertOK(response);
    }

    @Test
    @DisplayName("Connect to server with invalid password")
    void testLoginWithInvalidPassword() throws Exception {
        startServer();
        client = getClient(port);
        Response response = client.login("qwerty");

        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    void testLoginWhenLoggedIn() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        Response response = client.login(password);

        assertError(response, AlreadyLoggedInException.class);
        waitForState(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("Disconnects when not connected")
    void testDisconnectWhenNotConnected() throws Exception {
        startServer();
        client = getClient(port);
        Response response = client.logout();

        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Relog")
    void testRelogin() throws Exception {
        startServer();
        client = getClient(port);
        Response response = client.login(password);
        assertOK(response);
        Response response2 = client.logout();
        assertEquals(Status.OK, response2.getStatus());
        waitForState(ServerState.WAITING);
        client2 = getClient(port);
        Response response3 = client2.login(password);

        assertOK(response3);
    }

    @Test
    @DisplayName("Connects to server using invalid port")
    void testConnectToInvalidPort() throws Exception {
        startServer();
        assertThrows(ConnectException.class, () -> client = new JsonClient(4321));
    }

    @Test
    @DisplayName("Sends action without login")
    void testSendActionWithoutLogin() throws Exception {
        startServer();
        client = getClient(port);
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Send action after logout")
    void testSendActionAfterLogout() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        client.logout();
        waitForState(ServerState.WAITING);
        client2 = getClient(port);
        Response response = client2.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Sends action")
    void testSendAction() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
        assertOK(response);
    }

    @Test
    @DisplayName("Send multiple actions")
    void testSendMultipleActions() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
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
    void testConnectTwoClients() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        assertThrows(AssertionFailedError.class, () -> assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = getClient(port);
            client2.login(password);
        }));
        Mockito.verify(server, Mockito.times(1)).login();
    }

    @Test
    @DisplayName("Server does not restart after stop action")
    void testStopAction() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        Response response = client.doAction(new ServerStopAction());
        assertOKDisconnected(response);
        waitForState(ServerState.FORCED_SHUTDOWN);
        Thread.sleep(200);
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Reconnect after connection lost")
    void testReconnectAfterConnectionLost() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
        client.close();
        assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = getClient(port);
            Response login = client2.login(password);
            assertEquals(Status.OK, login.getStatus());
        });

        Mockito.verify(server, Mockito.times(2)).login();
    }

    @Test
    @DisplayName("Actions are executed in order")
    void testActionsInOrder() throws Exception {
        startServer();
        client = getClient(port);
        client.login(password);
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
    void testUpdateStateInUI() throws Exception {
        startServer();
        waitForState(ServerState.WAITING);
        Mockito.verify(window).updateUI(ServerState.WAITING);
    }

    /**
     * Starts server with default settings.
     *
     * @throws InterruptedException the InterruptedException
     */

    private void startServer() throws InterruptedException {
        serverRunner.start();
        waitForState(ServerState.WAITING);
    }

    /**
     * Waits for server to change state.
     *
     * @param state target state
     * @throws InterruptedException
     */
    private void waitForState(ServerState state) throws InterruptedException {
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            System.out.println(String.format("Waiting for state %s, current %s", state, serverRunner.getStatus()));
            return serverRunner.getStatus() == state;
        });
        assertEquals(state, serverRunner.getStatus(), "Invalid server state");
    }

    /**
     * Checks if response was invalid.
     *
     * @param response response
     * @param e        expected exception
     */
    private void assertError(Response response, Class<? extends Exception> e) {
        assertEquals(Status.NOT_OK, response.getStatus());
        assertNotNull(response.getException());
        assertEquals(e.getSimpleName(), response.getException());
    }

    /**
     * Checks if response and server state are OK.
     *
     * @param response server response
     */
    private void assertOK(Response response) {
        assertEquals(Status.OK, response.getStatus());
        assertEquals(ServerState.CONNECTED, server.getStatus());
    }

    /**
     * Checks if response is OK
     *
     * @param response server response
     */
    private void assertOKDisconnected(Response response) {
        assertEquals(Status.OK, response.getStatus());
    }

}
