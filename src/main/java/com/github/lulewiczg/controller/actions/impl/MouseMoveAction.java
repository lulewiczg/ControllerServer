package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.common.FakeRobot;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;

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
        if (robot instanceof FakeRobot) {
            return new Response(Status.OK);
        }
        // Java Robot is buggy
        POINT p = new POINT();
        User32.INSTANCE.GetCursorPos(p);
        User32.INSTANCE.SetCursorPos((long) (p.x + dx), (long) (p.y + dy));
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
