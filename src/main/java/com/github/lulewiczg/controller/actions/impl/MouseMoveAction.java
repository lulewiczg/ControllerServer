package com.github.lulewiczg.controller.actions.impl;

import java.awt.MouseInfo;
import java.awt.Point;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action for mouse move event.
 *
 * @author Grzegurz
 */
public class MouseMoveAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;
    private double dx;
    private double dy;

    public MouseMoveAction(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        Point p = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove((int) (p.x + dx), (int) (p.y + dy));
        return new Response(Status.OK);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", dx:").append(dx).append(", dy:").append(dy);
        return str.toString();
    }
}
