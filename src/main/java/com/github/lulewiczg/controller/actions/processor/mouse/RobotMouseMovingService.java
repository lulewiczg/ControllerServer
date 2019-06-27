package com.github.lulewiczg.controller.actions.processor.mouse;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Service for moving mouse cursor using Robot. Used when system is not Windows.
 *
 * @author Grzegurz
 */
@Service
@ConditionalOnMissingBean(JNAMouseMovingService.class)
public class RobotMouseMovingService implements MouseMovingService {

    @Autowired
    private Robot robot;

    /**
     * @see com.github.lulewiczg.controller.actions.processor.mouse.MouseMovingService#move(long, long)
     */
    @Override
    public void move(long dx, long dy) {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        int x = (int) b.getX();
        int y = (int) b.getY();
        robot.mouseMove((int) (x + dx), (int) (y + dy));
    }
}
