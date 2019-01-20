package com.github.lulewiczg.controller.actions;

public class MouseButtonReleaseAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    public MouseButtonReleaseAction(int key) {
        super(key);
    }

    @Override
    public void doAction() {
        robot.mouseRelease(key);
    }

}
