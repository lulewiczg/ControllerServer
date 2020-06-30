package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.mockito.Mockito.*;

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
        verify(server, never()).logout();
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);
        verify(server, times(1)).logout();
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(server, never()).logout();
        assertStatusNotOK(AuthorizationException.class);
    }
}
