package com.github.lulewiczg.controller.common;

import java.awt.AWTException;
import java.awt.Robot;

/**
 * Fake robot for testing purpose.
 *
 * @author Grzegurz
 */
public class FakeRobot extends Robot {

    public FakeRobot() throws AWTException {
        super();
    }

    @Override
    public synchronized void keyPress(int keycode) {
        // Do nothing
    }

    @Override
    public synchronized void keyRelease(int keycode) {
        // Do nothing
    }

    @Override
    public synchronized void mouseMove(int x, int y) {
        // Do nothing
    }

    @Override
    public synchronized void mousePress(int buttons) {
        // Do nothing
    }

    @Override
    public synchronized void mouseRelease(int buttons) {
        // Do nothing
    }

    @Override
    public synchronized void mouseWheel(int wheelAmt) {
        // Do nothing
    }
}
