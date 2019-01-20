package com.github.lulewiczg.controller.actions;

import java.awt.event.KeyEvent;

public class KeyPressAction extends KeyAction {

    private static final long serialVersionUID = 1L;

    public KeyPressAction(char c) {
        super(c);
    }

    @Override
    public void doAction() {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        robot.keyPress(key);
    }
}
