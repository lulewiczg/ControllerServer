package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

public class MouseButtonPressAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    protected MouseButtonPressAction() {
        super();
    }

    public MouseButtonPressAction(int key) {
        super(key);
    }

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        robot.mousePress(key);
        return new Response(Status.OK);
    }

}
