package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

public class MouseButtonReleaseAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    public MouseButtonReleaseAction(int key) {
        super(key);
    }

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        robot.mouseRelease(key);
        return new Response(Status.OK);
    }

}
