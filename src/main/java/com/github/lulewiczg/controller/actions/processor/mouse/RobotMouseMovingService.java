package com.github.lulewiczg.controller.actions.processor.mouse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;

/**
 * Service for moving mouse cursor using Robot. Used when system is not Windows.
 *
 * @author Grzegurz
 */
@Service
@ConditionalOnMissingBean(JNAMouseMovingService.class)
public class RobotMouseMovingService implements MouseMovingService {

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
