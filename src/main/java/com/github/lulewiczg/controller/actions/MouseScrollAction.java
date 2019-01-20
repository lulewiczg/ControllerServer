package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

public class MouseScrollAction extends Action {

    private static final long serialVersionUID = 1L;
    protected int lines;

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        robot.mouseWheel(lines);
        return new Response(Status.OK);
    }

    public MouseScrollAction(int lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", ").append(lines);
        return str.toString();
    }

}
