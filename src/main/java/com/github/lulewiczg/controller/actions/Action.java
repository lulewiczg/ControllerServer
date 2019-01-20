package com.github.lulewiczg.controller.actions;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.Serializable;

public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static Robot robot;

    public abstract void doAction();

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Action: ").append(this.getClass().getSimpleName());
        return str.toString();
    }

}
