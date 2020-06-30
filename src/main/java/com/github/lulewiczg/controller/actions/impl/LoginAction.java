package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.EnumSet;

/**
 * Aciton for login.
 *
 * @author Grzegurz
 */
@Log4j2
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginAction extends Action {

    private static final long serialVersionUID = 1L;
    private String password;
    private String info;
    private String ip;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#getProperStates()
     */
    @Override
    protected EnumSet<ServerState> getProperStates() {
        return EnumSet.of(ServerState.WAITING);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.actions.processor.ControllingService)
     */
    @Override
    protected Response doAction(ControllingService controllingService) {
        if (password == null || !password.equals(controllingService.getSettings().getPassword())) {
            throw new LoginException(info, ip);
        }
        log.info("Connected: {}, {}", info, ip);
        return new Response(Status.OK, ControllerServer::login);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doThrowException()
     */
    @Override
    protected void doThrowException() throws ActionException {
        throw new AlreadyLoggedInException("User already logged in");
    }

}
