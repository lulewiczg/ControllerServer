package com.github.lulewiczg.controller.actions.impl;

import static org.junit.Assert.assertThat;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockServerConfiguration;
import com.github.lulewiczg.controller.TestUtilConfiguration;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.ClientConnection;
import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;

/**
 * Tests template for actions.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockServerConfiguration.class, MockPropertiesConfiguration.class, TestUtilConfiguration.class,
        ActionProcessor.class })
@EnableAutoConfiguration
abstract class ActionTestTemplate {

    @Autowired
    protected ControllerServer server;

    @MockBean
    protected ClientConnection connection;

    @Autowired
    protected Robot robot;

    @Autowired
    protected JNAMouseMovingService mouseMovingService;

    @Autowired
    protected Clipboard clipboard;

    @Autowired
    protected ActionProcessor processor;

    /**
     * Returns action to test.
     *
     * @return action
     */
    protected abstract Action getAction();

    @BeforeEach
    void before() throws Exception {
        Mockito.when(connection.getNext()).thenReturn(getAction());
        additionalBefore();
    }

    /**
     * Additional setup logic;
     *
     * @throws Exception
     *             the Exception
     */
    protected void additionalBefore() throws Exception {
    }

    /**
     * Tests action in WAITING state.
     *
     * @throws Exception
     *             the Exception
     */
    protected abstract void doTestInWaiting() throws Exception;

    /**
     * Tests action in CONNECTED state.
     *
     * @throws Exception
     *             the Exception
     */
    protected abstract void doTestInConencted() throws Exception;

    /**
     * Tests action in SHUTDOWN state.
     *
     * @throws Exception
     *             the Exception
     */
    protected abstract void doTestInShutdown() throws Exception;

    @Test
    @DisplayName("Test action in WAITING state")
    void testInWaitingState() throws Exception {
        Mockito.when(server.getStatus()).thenReturn(ServerState.WAITING);
        doTestInWaiting();
    }

    @Test
    @DisplayName("Test action in SHUTDOWN state")
    void testInShutdownState() throws Exception {
        Mockito.when(server.getStatus()).thenReturn(ServerState.SHUTDOWN);
        doTestInShutdown();
    }

    @Test
    @DisplayName("Test action in CONNECTED state")
    void testinConnectedState() throws Exception {
        Mockito.when(server.getStatus()).thenReturn(ServerState.CONNECTED);
        doTestInConencted();
    }

    /**
     * Test if status is NOT_OK with given exception.
     *
     * @param ex
     *            expected exception
     * @throws IOException
     *             the IOException
     */
    protected void assertStatusNotOK(Class<? extends Exception> ex) throws IOException {
        assertStatus(Status.NOT_OK, ex);
    }

    /**
     * Test if status and exception are as expected.
     *
     * @param ex
     *            expected exception
     * @throws IOException
     *             the IOException
     */
    protected void assertStatus(Status status, Class<? extends Exception> ex) throws IOException {
        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        Mockito.verify(connection).write(argument.capture());
        assertThat(argument.getValue().getStatus(), Matchers.is(status));
        assertThat(argument.getValue().getException(), Matchers.is(ex.getSimpleName()));
    }

    /**
     * Test if status is OK.
     *
     * @throws IOException
     *             the IOException
     */
    protected void assertStatusOK() throws IOException {
        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        Mockito.verify(connection).write(argument.capture());
        assertThat(argument.getValue().getStatus(), Matchers.is(Status.OK));
        assertThat(argument.getValue().getException(), Matchers.equalTo(null));
    }
}
