package com.github.lulewiczg.controller.actions.impl;

import java.util.EnumSet;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.AlreadyLoggedInAction;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ServerState;

/**
 * Aciton for login.
 *
 * @author Grzegurz
 */
public class LoginAction extends Action {

    private static final long serialVersionUID = 1L;
    private String password;
    private String info;
    private String ip;

    public LoginAction(String password, String info, String ip) {
        this.password = password;
        this.info = info;
        this.ip = ip;
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#getProperStates()
     */
    @Override
    protected EnumSet<ServerState> getProperStates() {
        return EnumSet.of(ServerState.WAITING);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        if (password == null || !password.equals(controllingService.getSettings().getSettings().getPassword())) {
            throw new LoginException(info, ip);
        }
        log.info(String.format("Connected: %s, %s", info, ip));
        return new Response(Status.OK, i -> i.login());
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doThrowException()
     */
    @Override
    protected void doThrowException() throws ActionException {
        throw new AlreadyLoggedInAction("User already logged in");
    }

}
