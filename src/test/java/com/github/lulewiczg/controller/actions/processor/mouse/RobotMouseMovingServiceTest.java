package com.github.lulewiczg.controller.actions.processor.mouse;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.awt.*;

import static org.mockito.Mockito.verify;

/**
 * Tests for RobotMouseMovingService.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testLinux")
@SpringBootTest(classes = { AWTTestConfiguration.class, RobotMouseMovingService.class })
@EnableAutoConfiguration
class RobotMouseMovingServiceTest {

    @MockBean
    private Robot robot;

    @Autowired
    private RobotMouseMovingService service;

    @Test
    @DisplayName("Mouse move")
    void testMouseMove() {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        int x = (int) b.getX();
        int y = (int) b.getY();
        service.move(10, 20);
        verify(robot).mouseMove(x + 10, y + 20);
    }

}
