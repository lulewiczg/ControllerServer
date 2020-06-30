package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.MouseButtonAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Action for mouse button release event.
 *
 * @author Grzegurz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MouseButtonReleaseAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    private int key;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.actions.processor.ControllingService)
     */
    @Override
    protected Response doAction(ControllingService controllingService) {
        controllingService.getRobot().mouseRelease(key);
        return new Response(Status.OK);
    }

    @Override
    public String toString() {
        return super.toString() + key;
    }
}
