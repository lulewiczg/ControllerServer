package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.ServerExitException;

/**
 * Action that forces server to stop.
 *
 * @author Grzegurz
 */
public class ServerStopAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        return new Response(Status.OK, i -> {
            throw new ServerExitException();
        });
    }

}
