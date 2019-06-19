package com.github.lulewiczg.controller.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;
import com.github.lulewiczg.controller.actions.impl.MouseButtonPressAction;
import com.github.lulewiczg.controller.actions.processor.ObjectStreamActionProcessor;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInAction;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { TestConfiguration.class, ObjectStreamActionProcessor.class })
@EnableAutoConfiguration
public class ControllerServerTest {
    private static final int PORT = 5555;
    private static final String PASSWORD = "1234";

    private Client client;
    private Client client2;

    @Autowired
    private ControllerServer server;

    /**
     * Stops server after test.
     *
     * @throws InterruptedException
     *             the InterruptedException
     * @throws IOException
     */
    @AfterEach
    public void after() throws InterruptedException, IOException {
        server.stop();
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
    @DisplayName("Server not restart after shutdown")
    public void testServerShutdownAfterStop() throws Exception {
        startServer(false);
        client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();
        waitForState(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server is stopped after stop")
    public void testServerStateAfterStop() throws Exception {
        startServer(false);
        server.stop();
        waitForState(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server losts connection to client")
    public void testServerConnectionLost() throws Exception {
        startServer(false);
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        waitForState(ServerState.SHUTDOWN);
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
        startServer(true);
        client = new Client(PORT);
        Response response = client.login(PASSWORD);
        assertOK(response);
    }

    @Test
    @DisplayName("Connect to server wiith invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        startServer(true);
        client = new Client(PORT);
        Response response = client.login("4321");
        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    public void testLoginWhenLoggedIn() throws Exception {
        startServer(true);
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.login(PASSWORD);
        assertError(response, AlreadyLoggedInAction.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        startServer(true);
        client = new Client(PORT);
        Response response = client.logout();
        assertError(response, ActionException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Relog")
    public void testRelogin() throws Exception {
        startServer(true);
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
        startServer(true);
        assertThrows(ConnectException.class, () -> client = new Client(4321));
    }

    @Test
    @DisplayName("Sends action without login")
    public void testSendActionWithoutLogin() throws Exception {
        startServer(true);
        client = new Client(PORT);
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, AuthorizationException.class);
        waitForState(ServerState.WAITING);
    }

    @Test
    @DisplayName("Sends action after logout")
    public void testSendActionAfterLogout() throws Exception {
        startServer(true);
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
        startServer(true);
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
        assertOK(response);
    }

    @Test
    @DisplayName("Sends multiple actions")
    public void testSendMultipleActions() throws Exception {
        startServer(true);
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
        startServer(true);
        client = new Client(PORT);
        client.login(PASSWORD);
        assertThrows(AssertionFailedError.class, () -> assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(PORT);
            client2.login(PASSWORD);
        }));
    }

    @Test
    @DisplayName("Reconnect after connection lose")
    public void testServerStateAfterConnectionLost() throws Exception {
        startServer(true);
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        assertTimeoutPreemptively(Duration.ofMillis(200), () -> {
            client2 = new Client(PORT);
            Response login = client2.login(PASSWORD);
            assertEquals(Status.OK, login.getStatus());
        });
    }

    /**
     * Starts server with default settings.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */
    private void startServer(boolean restartAfterError) throws InterruptedException {
        server.start(new Settings(PORT, PASSWORD, true, restartAfterError));
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
        for (int i = 0; i < 5 && server.getStatus() != state; i++) {
            Thread.sleep(100);
        }
        assertEquals(state, server.getStatus(), "Invalid server state");
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
