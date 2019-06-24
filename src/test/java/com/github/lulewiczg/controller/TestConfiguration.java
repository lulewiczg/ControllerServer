package com.github.lulewiczg.controller;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.actions.processor.connection.VoidConnection;
import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.actions.processor.mouse.RobotMouseMovingService;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

@Profile("test")
@Configuration
@ImportAutoConfiguration(value = { ControllerServer.class, ControllerServerManager.class, ControllingService.class,
        SettingsComponent.class, ExceptionLoggingService.class, ActionProcessor.class, JNAMouseMovingService.class,
        RobotMouseMovingService.class, VoidConnection.class })
public class TestConfiguration {

    @MockBean
    private Robot robot;

    @MockBean
    private Clipboard clipboard;

    @SpyBean
    private ControllerServer server;

    @MockBean
    private SettingsComponent settings;

    @MockBean
    private JNAMouseMovingService mouseMovingService;

}
