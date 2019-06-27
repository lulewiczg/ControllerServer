package com.github.lulewiczg.controller.actions.processor.mouse;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.AWTSpringApplicationContextLoader;
import com.github.lulewiczg.controller.LightTestConfiguration;

/**
 * Tests for RobotMouseMovingService.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testLinux")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { LightTestConfiguration.class, RobotMouseMovingService.class })
@ContextConfiguration(loader = AWTSpringApplicationContextLoader.class)
@EnableAutoConfiguration
public class RobotMouseMovingServiceTest {

    @MockBean
    private Robot robot;

    @Autowired
    private RobotMouseMovingService service;

    @Test
    @DisplayName("Mouse move")
    public void testStateAfterLogout() throws Exception {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        int x = (int) b.getX();
        int y = (int) b.getY();
        service.move(10, 20);
        Mockito.verify(robot).mouseMove(x + 10, y + 20);
    }

}
