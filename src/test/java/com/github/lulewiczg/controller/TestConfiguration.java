package com.github.lulewiczg.controller;

import java.awt.AWTException;
import java.awt.Robot;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.lulewiczg.controller.actions.MouseMovingService;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.server.ControllerServer;

@Profile("test")
@Configuration()
@ImportAutoConfiguration(value = { ControllerServer.class, MouseMovingService.class, ControllingService.class })
public class TestConfiguration {

    @Bean
    public Robot robot() throws AWTException {
        Robot robot = Mockito.mock(Robot.class);
        Mockito.doNothing().when(robot).keyPress(Mockito.anyInt());
        Mockito.doNothing().when(robot).keyRelease(Mockito.anyInt());
        Mockito.doNothing().when(robot).mouseMove(Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(robot).mousePress(Mockito.anyInt());
        Mockito.doNothing().when(robot).mouseRelease(Mockito.anyInt());
        Mockito.doNothing().when(robot).mouseWheel(Mockito.anyInt());
        return robot;
    }
}
