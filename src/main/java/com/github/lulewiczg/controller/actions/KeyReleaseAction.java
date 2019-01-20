package com.github.lulewiczg.controller.actions;

import java.awt.event.KeyEvent;

import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

public class KeyReleaseAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    public KeyReleaseAction(char c) {
        super(c);
    }

    @Override
    protected Response doAction(ControllerServer server) throws ActionException {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        robot.keyRelease(key);
        return new Response(Status.OK);
    }

}
