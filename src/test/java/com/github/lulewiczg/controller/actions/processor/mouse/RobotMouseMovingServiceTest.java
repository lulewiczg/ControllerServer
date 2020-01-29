package com.github.lulewiczg.controller.actions.processor.mouse;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.AWTTestConfiguration;

/**
 * Tests for RobotMouseMovingService.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testLinux")
@SpringBootTest(classes = { AWTTestConfiguration.class, RobotMouseMovingService.class })
@EnableAutoConfiguration
public class RobotMouseMovingServiceTest {

    @MockBean
    private Robot robot;

    @Autowired
    private RobotMouseMovingService service;

    @Test
    @DisplayName("Mouse move")
    public void testMoueMove() throws Exception {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        int x = (int) b.getX();
        int y = (int) b.getY();
        service.move(10, 20);
        Mockito.verify(robot).mouseMove(x + 10, y + 20);
    }

}
