package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Action for mouse move event.
 *
 * @author Grzegurz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MouseMoveAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;
    private long dx;
    private long dy;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.actions.processor.ControllingService)
     */
    @Override
    protected Response doAction(ControllingService controllingService) {
        controllingService.getMouseService().move(dx, dy);
        return new Response(Status.OK);
    }

    @Override
    public String toString() {
        return super.toString() + ", dx:" + dx + ", dy:" + dy;
    }
}
