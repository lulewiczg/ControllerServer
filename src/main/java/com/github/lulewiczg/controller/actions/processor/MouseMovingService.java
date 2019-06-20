package com.github.lulewiczg.controller.actions.processor;

import org.springframework.stereotype.Service;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;

/**
 * Class for performing mouse move.
 *
 * @author Grzegurz
 */
@Service
public class MouseMovingService {

    /**
     * Moves mouse by given coordinates
     *
     * @param dx
     *            x
     * @param dy
     *            y
     */
    public void move(long dx, long dy) {
        POINT p = new POINT();
        User32.INSTANCE.GetCursorPos(p);
        User32.INSTANCE.SetCursorPos(p.x + dx, p.y + dy);
    }
}
