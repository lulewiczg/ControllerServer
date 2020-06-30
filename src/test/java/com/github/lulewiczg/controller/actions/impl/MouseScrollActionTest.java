package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests MouseScrollAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class MouseScrollActionTest extends ActionTestTemplate {

    private static final int LINES = 333;

    @Override
    protected Action getAction() {
        return new MouseScrollAction(LINES);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mouseWheel(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(robot).mouseWheel(LINES);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mouseWheel(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
