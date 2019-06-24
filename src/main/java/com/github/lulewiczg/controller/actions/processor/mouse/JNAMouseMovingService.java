package com.github.lulewiczg.controller.actions.processor.mouse;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;

/**
 * Service for moving mouse cursor using JNA. AWT robot is causing mouse sensivity reset. Works only on Windows systems.
 *
 * @author Grzegurz
 */
@Service
@Conditional(WindowsSystemCondition.class)
public class JNAMouseMovingService implements MouseMovingService {

    /**
     * @see com.github.lulewiczg.controller.actions.processor.mouse.MouseMovingService#move(long, long)
     */
    @Override
    public void move(long dx, long dy) {
        POINT p = new POINT();
        User32.INSTANCE.GetCursorPos(p);
        User32.INSTANCE.SetCursorPos(p.x + dx, p.y + dy);
    }
}
