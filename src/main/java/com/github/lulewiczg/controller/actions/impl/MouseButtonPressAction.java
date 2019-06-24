package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.MouseButtonAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;

/**
 * Action for mouse button press event.
 *
 * @author Grzegurz
 */
public class MouseButtonPressAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    public MouseButtonPressAction(int key) {
        super(key);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        controllingService.getRobot().mousePress(key);
        return new Response(Status.OK);
    }

}
