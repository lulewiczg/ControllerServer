package com.github.lulewiczg.controller.actions;

import java.awt.event.KeyEvent;

public class KeyReleaseAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    public KeyReleaseAction(char c) {
        super(c);
    }

    @Override
    public void doAction() {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        robot.keyRelease(key);
    }

}
