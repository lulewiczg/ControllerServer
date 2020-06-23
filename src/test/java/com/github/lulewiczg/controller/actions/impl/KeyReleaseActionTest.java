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
class KeyReleaseActionTest extends ActionTestTemplate {

    private static final int KEY = 321;

    @Override
    protected Action getAction() {
        return new KeyReleaseAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).keyRelease(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot).keyRelease(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).keyRelease(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
