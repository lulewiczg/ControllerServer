package com.github.lulewiczg.controller.actions;

import java.util.EnumSet;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.LoginException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;

public class LoginAction extends Action {

    private static final long serialVersionUID = 1L;
    private String password;
    private String info;
    private String ip;

    public LoginAction() {
        this.states = EnumSet.of(ServerState.WAITING);
    }

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        if (password == null || !password.equals(server.getPassword())) {
            throw new LoginException(info, ip);
        }
        log.info(String.format("Connected: %s, %s", info, ip));
        server.setStatus(ServerState.CONNECTED);
        return new Response(Status.OK);
    }

    public LoginAction(String password, String info, String ip) {
        this.password = password;
        this.info = info;
        this.ip = ip;
        this.states = EnumSet.of(ServerState.WAITING);
    }

}
