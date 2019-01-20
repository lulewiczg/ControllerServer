package com.github.lulewiczg.controller.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.ConnectException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.lulewiczg.controller.actions.MouseButtonPressAction;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.LoginException;

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
     */
    @AfterEach
    public void after() {
        server.stop();
    }

    @Test
    @DisplayName("Connect to server in down state")
    public void testConnectToDownServer() throws Exception {
        assertThrows(ConnectException.class, () -> new Client(PORT));
        assertEquals(ServerState.CONNECTION_ERROR, server.getStatus());
    }

    @Test
    @DisplayName("Connect to server in up state")
    public void testLoginToUpServer() throws Exception {
        server.start(PORT, PASSWORD, true);
        Response response = new Client(PORT).login(PASSWORD);
        assertOK(response);
    }

    @Test
    @DisplayName("Connect to server wiith invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        server.start(PORT, PASSWORD, true);
        Response response = new Client(PORT).login("4321");
        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    public void testLoginWhenLoggedIn() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.login(PASSWORD);
        assertError(response, LoginException.class);
        assertEquals(ServerState.CONNECTED, server.getStatus());
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        Response response = client.logout();
        assertError(response, ConnectException.class);
        assertEquals(ServerState.SHUTDOWN, server.getStatus());
    }

    @Test
    @DisplayName("Relog")
    public void testRelogin() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        Response response = client.login(PASSWORD);
        assertOK(response);
        Response response2 = client.logout();
        Thread.sleep(200);
        assertEquals(Status.OK, response2.getStatus());
        assertEquals(ServerState.SHUTDOWN, server.getStatus());
        Response response3 = new Client(PORT).login(PASSWORD);
        assertOK(response3);
    }

    @Test
    @DisplayName("Connects to server using invalid port")
    public void testConnectToInvalidPort() throws Exception {
        server.start(PORT, PASSWORD, true);
        assertThrows(ConnectException.class, () -> new Client(4321));
    }

    @Test
    @DisplayName("Sends action without login")
    public void testSendActionWithoutLogin() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, ConnectException.class);
        assertEquals(ServerState.CONNECTION_ERROR, server.getStatus());
    }

    @Test
    @DisplayName("Sends action after logout")
    public void testSendActionAfterLogout() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        client.login(PASSWORD);
        client.logout();
        Response response = client.doAction(new MouseButtonPressAction(1));
        assertError(response, ConnectException.class);
        assertEquals(ServerState.CONNECTION_ERROR, server.getStatus());
    }

    @Test
    @DisplayName("Sends action")
    public void testSendAction() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
        assertOK(response);
    }

    @Test
    @DisplayName("Sends multiple actions")
    public void testSendMultipleAction() throws Exception {
        server.start(PORT, PASSWORD, true);
        Client client = new Client(PORT);
        client.login(PASSWORD);
        for (int i = 0; i < 10; i++) {
            Response response = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON1_DOWN_MASK));
            assertOK(response);
            Response response2 = client.doAction(new MouseButtonPressAction(InputEvent.BUTTON2_DOWN_MASK));
            assertOK(response2);
        }
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
