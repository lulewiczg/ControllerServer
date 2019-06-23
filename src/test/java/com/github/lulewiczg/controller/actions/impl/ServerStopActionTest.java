package com.github.lulewiczg.controller.actions.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerExitException;
import com.github.lulewiczg.controller.server.InternalServerState;

/**
 * Tests ServerStopAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class ServerStopActionTest extends ActionTestTemplate {

    @Override
    protected Action getAction() {
        return new ServerStopAction();
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).setInternalState(Mockito.any());
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        assertThrows(ServerExitException.class, () -> processor.processAction(server));
        Mockito.verify(server).setInternalState(InternalServerState.DOWN_AND_DONT_START);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).setInternalState(Mockito.any());
        assertStatusNotOK(AuthorizationException.class);
    }
}