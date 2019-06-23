package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests KeyPressAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class KeyPressActionTest extends ActionTestTemplate {

    private static final int KEY = 123;

    @Override
    protected Action getAction() {
        return new KeyPressAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).keyPress(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot).keyPress(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).keyPress(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
