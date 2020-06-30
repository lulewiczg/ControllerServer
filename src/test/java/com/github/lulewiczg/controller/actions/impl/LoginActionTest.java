package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

/**
 * Tests LoginAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class LoginActionTest extends ActionTestTemplate {

    private static final String INFO = "Hello there,";
    private static final String IP = "General Kenobi!";
    private static final String PASSWORD = "password";

    @MockBean
    private SettingsComponent settings;

    @Override
    protected Action getAction() {
        return new LoginAction(PASSWORD, INFO, IP);
    }

    @Test
    @DisplayName("Login with invalid password")
    void testLoginWithInvalidPassword() throws Exception {
        when(settings.getPassword()).thenReturn("different pwd");
        when(server.getStatus()).thenReturn(ServerState.WAITING);

        processor.processAction(server);

        verify(server, never()).login();
        assertStatus(Status.INVALID_PASSWORD, LoginException.class);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        when(settings.getPassword()).thenReturn(PASSWORD);

        processor.processAction(server);

        verify(server).login();
        assertStatusOK();
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);

        verify(server, never()).login();
        assertStatusNotOK(AlreadyLoggedInException.class);
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);

        verify(server, never()).login();
        assertStatusNotOK(AlreadyLoggedInException.class);
    }
}
