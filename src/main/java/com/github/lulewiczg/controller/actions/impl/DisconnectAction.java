package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.DisconnectException;

/**
 * Action to disconnect from server.
 *
 * @author Grzegurz
 */
public class DisconnectAction extends Action {

    private static final long serialVersionUID = 1L;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        throw new DisconnectException();
    }

    @Override
    protected void doThrowException() throws ActionException {
        throw new ActionException("Already disconnected");
    }

}
