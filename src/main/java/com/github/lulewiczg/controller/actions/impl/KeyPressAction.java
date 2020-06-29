package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.KeyAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Action for key press event.
 *
 * @author Grzegurz
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeyPressAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    protected int key;

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllingService controllingService) throws ActionException {
        controllingService.getRobot().keyPress(key);
        return new Response(Status.OK);
    }
}
