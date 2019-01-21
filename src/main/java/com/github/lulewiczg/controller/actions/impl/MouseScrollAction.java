package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action for mouse scroll event.
 *
 * @author Grzegurz
 */
public class MouseScrollAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;
    private int lines;

    public MouseScrollAction(int lines) {
        this.lines = lines;
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        robot.mouseWheel(lines);
        return new Response(Status.OK);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", ").append(lines);
        return str.toString();
    }

}
