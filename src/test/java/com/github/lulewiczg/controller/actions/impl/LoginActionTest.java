package com.github.lulewiczg.controller.actions.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.Settings;

/**
 * Tests LoginAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class LoginActionTest extends ActionTestTemplate {

    private static final String INFO = "Hello there,";
    private static final String IP = "General Kenobi!";
    private static final String PASSWORD = "password";
    private Settings passwordSetting;

    @Override
    public void additionalBefore() throws Exception {
        passwordSetting = new Settings();
        passwordSetting.setPassword(PASSWORD);
        Mockito.when(settings.getSettings()).thenReturn(passwordSetting);
    }

    @Override
    protected Action getAction() {
        return new LoginAction(PASSWORD, INFO, IP);
    }

    @Test
    @DisplayName("Login with invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        passwordSetting.setPassword("different pwd");
        Mockito.when(server.getStatus()).thenReturn(ServerState.WAITING);
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).login();
        assertStatus(Status.INVALID_PASSWORD, LoginException.class);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(server).login();
        assertStatusOK();
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).login();
        assertStatusNotOK(AlreadyLoggedInException.class);
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        Mockito.verify(server, Mockito.never()).login();
        assertStatusNotOK(AlreadyLoggedInException.class);
    }
}
