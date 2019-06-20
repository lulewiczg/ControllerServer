package com.github.lulewiczg.controller.actions.impl;

import java.awt.Robot;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import com.github.lulewiczg.controller.actions.LoginRequiredAction;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.common.Status;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;

/**
 * Action for sending text.
 *
 * @author Grzegurz
 */
public class TextAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;

    private String text;

    public TextAction(String text) {
        this.text = text;
    }

    /**
     * @see com.github.lulewiczg.controller.actions.Action#doAction(com.github.lulewiczg.controller.server.ControllerServer)
     */
    @Override
    protected Response doAction(ControllerServer server, ControllingService controllingService) throws ActionException {
        StringSelection stringSelection = new StringSelection(text);
        controllingService.getClipboard().setContents(stringSelection, null);
        Robot robot = controllingService.getRobot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        return new Response(Status.OK);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", ").append(text);
        return str.toString();
    }
}
