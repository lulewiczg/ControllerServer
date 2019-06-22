package com.github.lulewiczg.controller.server;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;
import com.github.lulewiczg.controller.actions.impl.KeyPressAction;
import com.github.lulewiczg.controller.actions.impl.KeyReleaseAction;
import com.github.lulewiczg.controller.actions.impl.MouseButtonPressAction;
import com.github.lulewiczg.controller.actions.impl.MouseButtonReleaseAction;
import com.github.lulewiczg.controller.actions.impl.MouseMoveAction;
import com.github.lulewiczg.controller.actions.impl.MouseScrollAction;
import com.github.lulewiczg.controller.actions.impl.TextAction;
import com.github.lulewiczg.controller.actions.processor.MouseMovingService;
import com.github.lulewiczg.controller.actions.processor.ObjectStreamActionProcessor;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
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
    private static final String TEST_TXT = "test txt";
    private static final int PORT = 5555;
    private static final String PASSWORD = "1234";

    private Client client;
    private Client client2;

    @SpyBean
    private ControllerServer server;

    @MockBean
    private SettingsComponent settings;

    @MockBean
    private Robot robot;

    @MockBean
    private MouseMovingService mouseMovingService;

    @MockBean
    private Clipboard clipboard;

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

        Mockito.verify(server, Mockito.never()).login();
        assertEquals(Status.INVALID_PASSWORD, response.getStatus());
    }

    @Test
    @DisplayName("Log in when already logged in")
    public void testLoginWhenLoggedIn() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        Response response = client.login(PASSWORD);

        Mockito.verify(server, Mockito.times(1)).login();
        assertError(response, AlreadyLoggedInException.class);
        waitForState(ServerState.CONNECTED);
    }

    @Test
    @DisplayName("Disconnects when not connected")
    public void testDisconnectWhenNotConnected() throws Exception {
        startServer();
        client = new Client(PORT);
        Response response = client.logout();

        Mockito.verify(server, Mockito.never()).logout();
        assertError(response, ActionException.class);
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

        Mockito.verify(server, Mockito.times(2)).login();
        Mockito.verify(server).logout();
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
    @DisplayName("Sends multiple actions")
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
    @DisplayName("Reconnect after connection lose")
    public void testServerStateAfterConnectionLost() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        client.close();
        assertTimeoutPreemptively(Duration.ofMillis(200), () -> {
            client2 = new Client(PORT);
            Response login = client2.login(PASSWORD);
            assertEquals(Status.OK, login.getStatus());
        });

        Mockito.verify(server, Mockito.times(2)).login();
    }

    @Test
    @DisplayName("Key action")
    public void testKeyAction() throws Exception {
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
    @DisplayName("Mouse button action")
    public void testMouseButtonAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        List<Response> responses = new ArrayList<>();
        responses.add(client.doAction(new MouseButtonPressAction(1)));
        responses.add(client.doAction(new MouseButtonPressAction(2)));
        responses.add(client.doAction(new MouseButtonReleaseAction(1)));
        responses.add(client.doAction(new MouseButtonReleaseAction(2)));

        responses.forEach(i -> assertOK(i));
        InOrder inOrder = Mockito.inOrder(robot);
        inOrder.verify(robot).mousePress(1);
        inOrder.verify(robot).mousePress(2);
        inOrder.verify(robot).mouseRelease(1);
        inOrder.verify(robot).mouseRelease(2);
    }

    @Test
    @DisplayName("Mouse wheel action")
    public void testMouseWheelAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        List<Response> responses = new ArrayList<>();
        responses.add(client.doAction(new MouseScrollAction(1)));
        responses.add(client.doAction(new MouseScrollAction(2)));
        responses.add(client.doAction(new MouseScrollAction(-5)));

        responses.forEach(i -> assertOK(i));
        InOrder inOrder = Mockito.inOrder(robot);
        inOrder.verify(robot).mouseWheel(1);
        inOrder.verify(robot).mouseWheel(2);
        inOrder.verify(robot).mouseWheel(-5);
    }

    @Test
    @DisplayName("Mouse move action")
    public void testMouseMoveAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        List<Response> responses = new ArrayList<>();
        responses.add(client.doAction(new MouseMoveAction(1, 2)));
        responses.add(client.doAction(new MouseMoveAction(3, 4)));
        responses.add(client.doAction(new MouseMoveAction(-100, 100)));
        responses.add(client.doAction(new MouseMoveAction(123, 321)));
        responses.add(client.doAction(new MouseMoveAction(0, 0)));

        responses.forEach(i -> assertOK(i));
        InOrder inOrder = Mockito.inOrder(mouseMovingService);
        inOrder.verify(mouseMovingService).move(1, 2);
        inOrder.verify(mouseMovingService).move(3, 4);
        inOrder.verify(mouseMovingService).move(-100, 100);
        inOrder.verify(mouseMovingService).move(123, 321);
        inOrder.verify(mouseMovingService).move(0, 0);
    }

    @Test
    @DisplayName("Text action")
    public void testTextAction() throws Exception {
        startServer();
        client = new Client(PORT);
        client.login(PASSWORD);
        List<Response> responses = new ArrayList<>();
        responses.add(client.doAction(new TextAction(TEST_TXT)));

        responses.forEach(i -> assertOK(i));
        ArgumentCaptor<StringSelection> argument = ArgumentCaptor.forClass(StringSelection.class);
        InOrder inOrder = Mockito.inOrder(clipboard, robot);
        inOrder.verify(clipboard).setContents(argument.capture(), Mockito.eq(null));
        assertThat(argument.getValue().getTransferData(DataFlavor.stringFlavor),
                Matchers.equalTo(new StringSelection(TEST_TXT).getTransferData(DataFlavor.stringFlavor)));
        inOrder.verify(robot).keyPress(KeyEvent.VK_CONTROL);
        inOrder.verify(robot).keyPress(KeyEvent.VK_V);
        inOrder.verify(robot).keyRelease(KeyEvent.VK_V);
        inOrder.verify(robot).keyRelease(KeyEvent.VK_CONTROL);
    }

    /**
     * Starts server with default settings.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */

    private void startServer() throws InterruptedException {
        Settings s = new Settings();
        s.setAutostart(true);
        s.setPassword(PASSWORD);
        s.setPort(PORT);
        Mockito.when(settings.getSettings()).thenReturn(s);

        server.start();
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
