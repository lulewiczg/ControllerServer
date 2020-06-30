package com.github.lulewiczg.controller.actions.processor.mouse;

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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests ActionProcessor class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockServerConfiguration.class, MockPropertiesConfiguration.class, TestUtilConfiguration.class,
        ActionProcessor.class })
@EnableAutoConfiguration
class ActionProcessorTest {

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
    void testActionIsInvoked() throws Exception {
        when(connection.getNext()).thenReturn(action);
        Response response = new Response(Status.OK);
        when(action.run(any(), any())).thenReturn(response);

        processor.processAction(server);

        verify(action).run(server, controllingService);
        verify(connection).write(response);
    }

    @Test
    @DisplayName("Response callback is invoked")
    void testCallbackIsInvoked() throws Exception {
        when(connection.getNext()).thenReturn(action);
        Response response = new Response(Status.OK, ControllerServer::getStatus);
        when(action.run(any(), any())).thenReturn(response);

        processor.processAction(server);

        InOrder inOrder = inOrder(action, connection, server);
        inOrder.verify(action).run(server, controllingService);
        inOrder.verify(connection).write(response);
        inOrder.verify(server).getStatus();
    }

    @Test
    @DisplayName("Action throws unexpected exception")
    void testActionUnhandledException() throws Exception {
        Exception expected = new RuntimeException(TEST);
        when(connection.getNext()).thenReturn(action);
        when(action.run(any(), any())).thenThrow(expected);

        Exception actual = assertThrows(expected.getClass(), () -> processor.processAction(server));

        assertThat(actual, Matchers.equalTo(expected));
        verify(connection).write(new Response(Status.NOT_OK));
    }

    @Test
    @DisplayName("Action throws ServerStopException")
    void testActionServerStopException() throws Exception {
        Exception expected = new ServerExitException();
        when(connection.getNext()).thenReturn(action);
        when(action.run(any(), any())).thenThrow(expected);

        Exception actual = assertThrows(expected.getClass(), () -> processor.processAction(server));

        assertThat(actual, Matchers.equalTo(expected));
        verify(connection, never()).write(any());
    }

    @MethodSource
    @DisplayName("Exceptions return proper status")
    @ParameterizedTest(name = "''{0}'' should return status ''{1}''")
    void testActionHandledException(Exception expected, Status status) throws Exception {
        when(connection.getNext()).thenReturn(action);
        when(action.run(any(), any())).thenThrow(expected);

        processor.processAction(server);

        verify(connection).write(new Response(status));
    }

    @Test
    @DisplayName("Processor close")
    void testClose() throws Exception {
        processor.close();

        verify(connection).close();
    }

    @Test
    @DisplayName("Error when sending response to client")
    void testConnectionError() throws Exception {
        when(connection.getNext()).thenReturn(action);
        doThrow(IOException.class).when(connection).write(any());
        Response response = new Response(Status.OK);
        when(action.run(any(), any())).thenReturn(response);

        processor.processAction(server);

        verify(connection).write(response);
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
