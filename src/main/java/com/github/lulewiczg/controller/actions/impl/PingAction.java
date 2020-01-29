package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;

/**
 * Ping action to keep connection alive.
 *
 * @author Grzegorz
 */
public class PingAction extends Action {

    private static final long serialVersionUID = 1L;

    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        // Do nothing
        return new Response(Status.OK);
    }

    @Override
    protected void doThrowException() throws ActionException {
        // Do nothing
    }

}
