package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests MouseButtonPressAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class MouseButtonPressActionTest extends ActionTestTemplate {

    private static final int KEY = 3;

    @Override
    protected Action getAction() {
        return new MouseButtonPressAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mousePress(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(robot).mousePress(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mousePress(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
