package com.github.lulewiczg.controller.actions.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerExitException;

/**
 * Tests ServerStopAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class ServerStopActionTest extends ActionTestTemplate {

    @Override
    protected Action getAction() {
        return new ServerStopAction();
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        assertThrows(ServerExitException.class, () -> processor.processAction(server));
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        assertStatusNotOK(AuthorizationException.class);
    }
}
