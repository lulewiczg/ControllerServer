package com.github.lulewiczg.controller.actions.impl;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests DisconnectAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class DisconnectActionTest extends ActionTestTemplate {

    @Override
    protected Action getAction() {
        return new DisconnectAction();
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).logout();
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.times(1)).logout();
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).logout();
        assertStatusNotOK(AuthorizationException.class);
    }
}
