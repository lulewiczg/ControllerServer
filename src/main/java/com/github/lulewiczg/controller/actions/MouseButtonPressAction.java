package com.github.lulewiczg.controller.actions;

public class MouseButtonPressAction extends MouseButtonAction {

    private static final long serialVersionUID = 1L;

    public MouseButtonPressAction(int key) {
        super(key);
    }

    @Override
    public void doAction() {
        robot.mousePress(key);
    }

}
