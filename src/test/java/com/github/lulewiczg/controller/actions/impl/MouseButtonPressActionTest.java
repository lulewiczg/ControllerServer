package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests MouseButtonPressAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class MouseButtonPressActionTest extends ActionTestTemplate {

    private static final int KEY = 3;

    @Override
    protected Action getAction() {
        return new MouseButtonPressAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).mousePress(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot).mousePress(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).mousePress(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
