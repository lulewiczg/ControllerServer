package com.github.lulewiczg.controller.actions.impl;

import java.awt.event.KeyEvent;

import com.github.lulewiczg.controller.actions.KeyAction;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action for key release event.
 *
 * @author Grzegurz
 *
 */
public class KeyReleaseAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    public KeyReleaseAction(char c) {
        super(c);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        robot.keyRelease(key);
        return new Response(Status.OK);
    }

}
