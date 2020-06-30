package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests MouseButtonReleaseAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class MouseButtonReleaseActionTest extends ActionTestTemplate {

    private static final int KEY = 5;

    @Override
    protected Action getAction() {
        return new MouseButtonReleaseAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mouseRelease(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(robot).mouseRelease(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(robot, never()).mouseRelease(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
