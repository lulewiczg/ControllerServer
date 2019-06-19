package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
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
    private long dx;
    private long dy;

    public MouseMoveAction(long dx, long dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server, ActionProcessor actionProcessor) throws ActionException {
        actionProcessor.getMouseService().move(dx, dy);
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
