package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action to disconnect from server.
 *
 * @author Grzegurz
 */
public class DisconnectAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.actions.processor.ControllingService)
     */
    @Override
    protected Response doAction(ControllingService controllingService) {
        return new Response(Status.OK, ControllerServer::logout);
    }

}
