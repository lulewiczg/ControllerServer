package com.github.lulewiczg.controller.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.github.lulewiczg.controller.AWTSpringApplicationContextLoader;
import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.MainConfiguration;
import com.github.lulewiczg.controller.TestUtilConfiguration;
import com.github.lulewiczg.controller.actions.impl.KeyPressAction;
import com.github.lulewiczg.controller.actions.impl.KeyReleaseAction;
import com.github.lulewiczg.controller.actions.impl.MouseButtonPressAction;
import com.github.lulewiczg.controller.actions.impl.ServerStopAction;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ObjectStreamClientConnection;
import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("testInteg")
@EnableAutoConfiguration
@ContextConfiguration(loader = AWTSpringApplicationContextLoader.class)
@SpringBootTest(classes = { AWTTestConfiguration.class, EagerConfiguration.class, MainConfiguration.class,
        ControllerServerManager.class, TestUtilConfiguration.class, JNAMouseMovingService.class, JTextAreaAppender.class,
        ControllerServer.class, ObjectStreamClientConnection.class, ActionProcessor.class })
public class ControllerServerIntegTest {

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

    /**
     * Stops server after test.
     *
     * @throws Exception
     *             the Exception
     */
    @AfterEach
    public void after() throws Exception {
        if (serverRunner.isRunning()) {
            serverRunner.stop();
        }
        waitForState(ServerState.FORCED_SHUTDOWN);
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
    public void before() {
        server.setStatus(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server restart after logout")
    public void testStateAfterLogout() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
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
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server can't be started twice")
    public void test() throws Exception {
        startServer();
        waitForState(ServerState.WAITING);
        assertThrows(ServerAlreadyRunningException.class, () -> startServer());
    }

    @Test
    @DisplayName("Server loses connection to client")
    public void testServerConnectionLost() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        client.close();
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Connect to server in down state")
    public void testConnectToDownServer() throws Exception {
        assertThrows(ConnectException.class, () -> new Client(port));
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Connect to server in up state")
    public void testLoginToUpServer() throws Exception {
        startServer();
        client = new Client(port);
        Response response = client.login(password);
        assertOK(response);
    }

    @Test
    @DisplayName("Connect to server wiith invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        startServer();
        client = new Client(port);
        Response response = client.login("qwerty");

        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    public void testLoginWhenLoggedIn() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        Response response = client.login(password);

        assertError(response, AlreadyLoggedInException.class);
        waitForState(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        startServer();
        client = new Client(port);
        Response response = client.logout();

        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Relog")
    public void testRelogin() throws Exception {
        startServer();
        client = new Client(port);
        Response response = client.login(password);
        assertOK(response);
        Response response2 = client.logout();
        assertEquals(Status.OK, response2.getStatus());
        waitForState(ServerState.WAITING);
        client2 = new Client(port);
        Response response3 = client2.login(password);

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
        client = new Client(port);
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Send action after logout")
    public void testSendActionAfterLogout() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        client.logout();
        waitForState(ServerState.WAITING);
        client2 = new Client(port);
        Response response = client2.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Sends action")
    public void testSendAction() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
        assertOK(response);
    }

    @Test
    @DisplayName("Send multiple actions")
    public void testSendMultipleActions() throws Exception {
        startServer();
        client = new Client(port);
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
    public void testConnectTwoClients() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        assertThrows(AssertionFailedError.class, () -> assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(port);
            client2.login(password);
        }));
        Mockito.verify(server, Mockito.times(1)).login();
    }

    @Test
    @DisplayName("Server does not restart after stop action")
    public void testStopAction() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        Response response = client.doAction(new ServerStopAction());
        assertOKDisconnected(response);
        waitForState(ServerState.FORCED_SHUTDOWN);
        Thread.sleep(200);
        waitForState(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Reconnect after connection lost")
    public void testReconnectAfterConnectionLost() throws Exception {
        startServer();
        client = new Client(port);
        client.login(password);
        client.close();
        assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(port);
            Response login = client2.login(password);
            assertEquals(Status.OK, login.getStatus());
        });

        Mockito.verify(server, Mockito.times(2)).login();
    }

    @Test
    @DisplayName("Actions are executed in order")
    public void testActionsInOrder() throws Exception {
        startServer();
        client = new Client(port);
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
        Awaitility.await().atMost(20000, TimeUnit.SECONDS).until(() -> {
            System.out.println(serverRunner.getStatus());
            return serverRunner.getStatus() == state;
        });
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

    /**
     * Checks if response is OK
     *
     * @param response
     *            server response
     */
    private void assertOKDisconnected(Response response) {
        assertEquals(Status.OK, response.getStatus());
    }

}
