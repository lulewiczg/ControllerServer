package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;

/**
 * Ping action to keep connection alive.
 *
 * @author Grzegorz
 */
public class PingAction extends Action {

    private static final long serialVersionUID = 1L;


    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.actions.processor.ControllingService)
     */
    @Override
    protected Response doAction(ControllingService controllingService) {
        return new Response(Status.OK);
    }

    @Override
    protected void doThrowException() {
        // Do nothing
    }

}
