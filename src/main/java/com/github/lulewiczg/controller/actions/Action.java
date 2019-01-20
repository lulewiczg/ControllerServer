package com.github.lulewiczg.controller.actions;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.Serializable;

import com.github.lulewiczg.controller.common.FakeRobot;

public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static Robot robot;

    public abstract void doAction();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Action: ").append(this.getClass().getSimpleName());
        return str.toString();
    }

    /**
     * Sets actions to test mode.
     */
    public static void setTestMode() {
        try {
            robot = new FakeRobot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets actions to normal mode.
     */
    public static void setNormalMode() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
}
