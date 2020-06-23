package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

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
        Mockito.verify(robot, Mockito.never()).mouseWheel(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot).mouseWheel(LINES);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).mouseWheel(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
