package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests MouseButtonReleaseAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class MouseButtonReleaseActionTest extends ActionTestTemplate {

    private static final int KEY = 5;

    @Override
    protected Action getAction() {
        return new MouseButtonReleaseAction(KEY);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).mouseRelease(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot).mouseRelease(KEY);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(robot, Mockito.never()).mouseRelease(Mockito.anyInt());
        assertStatusNotOK(AuthorizationException.class);
    }
}
