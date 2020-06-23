package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

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
        Mockito.verify(mouseMovingService, Mockito.never()).move(Mockito.anyInt(), Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(mouseMovingService).move(Mockito.eq(X), Mockito.eq(Y));
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(mouseMovingService, Mockito.never()).move(Mockito.anyInt(), Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
