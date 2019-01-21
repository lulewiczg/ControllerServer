package com.github.lulewiczg.controller.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.lulewiczg.controller.actions.MouseButtonPressAction;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 *
 */
public class ControllerServerTest {
    private static final int PORT = 5555;
    private static final String PASSWORD = "1234";

    private static ControllerServer server;
    private Client client;
    private Client client2;

    /**
     * Prepares test data.
     *
     * @throws IOException
     *             the IOException
     * @throws InterruptedException
     *             the InterruptedException
     */
    @BeforeAll
    public static void before() throws IOException, InterruptedException {
        server = ControllerServer.getInstance();
    }

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
    @DisplayName("Server not restart after shutodwn")
    public void testServerShutdownAfterStop() throws Exception {
        server.start(new Settings(PORT, PASSWORD, true, false, true));
        client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();
        Thread.sleep(200);
        assertEquals(ServerState.SHUTDOWN, server.getStatus());
    }

    @Test
    @DisplayName("Server losts connection to client")
    public void testServerConnectionLost() throws Exception {
        server.start(new Settings(PORT, PASSWORD, true, false, true));
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        Thread.sleep(200);
        assertEquals(ServerState.CONNECTION_ERROR, server.getStatus());
    }

    @Test
    @DisplayName("Connect to server in down state")
    public void testConnectToDownServer() throws Exception {
        assertThrows(ConnectException.class, () -> new Client(PORT));
        assertEquals(ServerState.SHUTDOWN, server.getStatus());
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
        assertError(response, null);
        assertEquals(ServerState.CONNECTED, server.getStatus());
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.logout();
        assertError(response, null);
        assertEquals(ServerState.WAITING, server.getStatus());
    }

    @Test
    @DisplayName("Relog")
    public void testRelogin() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.login(PASSWORD);
        assertOK(response);
        Response response2 = client.logout();
        Thread.sleep(200);
        assertEquals(Status.OK, response2.getStatus());
        assertEquals(ServerState.WAITING, server.getStatus());
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
        assertError(response, null);
        assertEquals(ServerState.WAITING, server.getStatus());
    }

    @Test
    @DisplayName("Sends action after logout")
    public void testSendActionAfterLogout() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();
        Thread.sleep(200);
        client2 = new Client(PORT);
        Response response = client2.doAction(new MouseButtonPressAction(1));
        assertError(response, null);
        assertEquals(ServerState.WAITING, server.getStatus());
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
    @DisplayName("Sends multiple actions")
    public void testSendMultipleAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        for (int i = 0; i < 10; i++) {
            Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
            Thread.sleep(20);
            assertOK(response);
            Response response2 = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON2_DOWN_MASK));
            Thread.sleep(20);
            assertOK(response2);
        }
    }

    // @Test
    @DisplayName("Two clients connect")
    public void testConnectTwoClients() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        assertThrows(AssertionFailedError.class, () -> assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            client2 = new Client(PORT);
            client2.login(PASSWORD);
        }));
    }

    /**
     * Starts server with default settings.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */
    private void startServer() throws InterruptedException {
        server.start(new Settings(PORT, PASSWORD, true, true, true));
        Thread.sleep(200);
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
        assertEquals(ActionException.class, response.getException().getClass());
        if (e != null) {
            assertEquals(e, response.getException().getCause().getClass());
        }
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
