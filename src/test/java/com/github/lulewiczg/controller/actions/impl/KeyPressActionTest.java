package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests KeyPressAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class KeyPressActionTest extends ActionTestTemplate {

    private static final int KEY = 123;

    @Override
    protected Action getAction() {
        return new KeyPressAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(robot, never()).keyPress(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(robot).keyPress(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(robot, never()).keyPress(anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
