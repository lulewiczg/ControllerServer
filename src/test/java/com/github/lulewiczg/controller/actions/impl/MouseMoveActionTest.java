package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests KeyReleaseAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class MouseMoveActionTest extends ActionTestTemplate {

    private static final long X = 567;
    private static final long Y = 890;

    @Override
    protected Action getAction() {
        return new MouseMoveAction(X, Y);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(mouseMovingService, Mockito.never()).move(anyInt(), anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(mouseMovingService).move(eq(X), eq(Y));
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(mouseMovingService, Mockito.never()).move(anyInt(), anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
