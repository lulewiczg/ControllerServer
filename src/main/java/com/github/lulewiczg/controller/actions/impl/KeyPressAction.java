package com.github.lulewiczg.controller.actions.impl;

import java.awt.event.KeyEvent;

import com.github.lulewiczg.controller.actions.KeyAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action for key press event.
 *
 * @author Grzegurz
 */
public class KeyPressAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    public KeyPressAction(char c) {
        super(c);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        robot.keyPress(key);
        return new Response(Status.OK);
    }
}
