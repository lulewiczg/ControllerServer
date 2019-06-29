package com.github.lulewiczg.controller;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Configuration for mocking properties.
 *
 * @author Grzegurz
 */
@Configuration
public class MockServerConfiguration {

    @MockBean
    private Robot robot;

    @MockBean
    private Clipboard clipboard;

    @SpyBean
    private ControllerServer server;

    @MockBean
    private JNAMouseMovingService mouseMovingService;

    @Bean
    @Scope("prototype")
    public ServerSocket serverSocket(SettingsComponent settings) throws IOException {
        return new ServerSocket(settings.getPort());
    }
}
