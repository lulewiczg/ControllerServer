package com.github.lulewiczg.controller.actions.processor.mouse;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockServerConfiguration;
import com.github.lulewiczg.controller.TestUtilConfiguration;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.exception.ServerExitException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Tests ActionProcessor class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { MockServerConfiguration.class, MockPropertiesConfiguration.class, TestUtilConfiguration.class,
        ActionProcessor.class })
@EnableAutoConfiguration
public class ActionProcessorTest {

    private static final String TEST = "test";

    @Autowired
    private ActionProcessor processor;

    @Autowired
    private ControllerServer server;

    @Autowired
    private ControllingService controllingService;

    @Mock
    private Action action;

    @MockBean
    private ClientConnection connection;

    @Test
    @DisplayName("Action is invoked")
    public void testActionIsInvoked() throws Exception {
        Mockito.when(connection.getNext()).thenReturn(action);
        Response response = new Response(Status.OK);
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenReturn(response);

        processor.processAction(server);

        Mockito.verify(action).run(server, controllingService);
        Mockito.verify(connection).write(response);
    }

    @Test
    @DisplayName("Response callback is invoked")
    public void testCallbackIsInvoked() throws Exception {
        Mockito.when(connection.getNext()).thenReturn(action);
        Response response = new Response(Status.OK, i -> i.getStatus());
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenReturn(response);

        processor.processAction(server);

        InOrder inOrder = Mockito.inOrder(action, connection, server);
        inOrder.verify(action).run(server, controllingService);
        inOrder.verify(connection).write(response);
        inOrder.verify(server).getStatus();
    }

    @Test
    @DisplayName("Action throws unexpected exception")
    public void testActionUnhandledException() throws Exception {
        Exception expected = new RuntimeException(TEST);
        Mockito.when(connection.getNext()).thenReturn(action);
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenThrow(expected);

        Exception actual = assertThrows(expected.getClass(), () -> processor.processAction(server));

        assertThat(actual, Matchers.equalTo(expected));
        Mockito.verify(connection).write(new Response(Status.NOT_OK));
    }

    @Test
    @DisplayName("Action throws ServerStopException")
    public void testActionServerStopException() throws Exception {
        Exception expected = new ServerExitException();
        Mockito.when(connection.getNext()).thenReturn(action);
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenThrow(expected);

        Exception actual = assertThrows(expected.getClass(), () -> processor.processAction(server));

        assertThat(actual, Matchers.equalTo(expected));
        Mockito.verify(connection, Mockito.never()).write(Mockito.any());
    }

    @MethodSource
    @DisplayName("Exceptions return proper status")
    @ParameterizedTest(name = "''{0}'' should return status ''{1}''")
    public void testActionHandledException(Exception expected, Status status) throws Exception {
        Mockito.when(connection.getNext()).thenReturn(action);
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenThrow(expected);

        processor.processAction(server);

        Mockito.verify(connection).write(new Response(status));
    }

    @Test
    @DisplayName("Processor close")
    public void testClose() throws Exception {
        processor.close();

        Mockito.verify(connection).close();
    }

    @Test
    @DisplayName("Error when sending response to client")
    public void testConnectionError() throws Exception {
        Mockito.when(connection.getNext()).thenReturn(action);
        Mockito.doThrow(IOException.class).when(connection).write(Mockito.any());
        Response response = new Response(Status.OK);
        Mockito.when(action.run(Mockito.any(), Mockito.any())).thenReturn(response);

        processor.processAction(server);

        Mockito.verify(connection).write(response);
    }

    /**
     * Creates test parameters.
     *
     * @return parameters
     */
    private static Stream<Arguments> testActionHandledException() {
        return Stream.of(Arguments.of(new LoginException(TEST, TEST), Status.INVALID_PASSWORD),
                Arguments.of(new AlreadyLoggedInException(TEST), Status.NOT_OK),
                Arguments.of(new AuthorizationException(TEST), Status.NOT_OK));
    }

}
