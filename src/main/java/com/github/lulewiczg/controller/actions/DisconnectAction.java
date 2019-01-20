package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.DisconnectException;
import com.github.lulewiczg.controller.server.ControllerServer;

public class DisconnectAction extends Action {

    private static final long serialVersionUID = 1L;

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        throw new DisconnectException();
    }

}
