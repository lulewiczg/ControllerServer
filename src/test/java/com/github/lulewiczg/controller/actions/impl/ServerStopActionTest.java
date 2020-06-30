package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import com.github.lulewiczg.controller.exception.ServerExitException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    protected void doTestInConnected() throws Exception {
        assertThrows(ServerExitException.class, () -> processor.processAction(server));
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        assertStatusNotOK(AuthorizationException.class);
    }
}
