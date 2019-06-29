package com.github.lulewiczg.controller;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

@Configuration
@ImportAutoConfiguration(value = { AWTTestConfiguration.class, MockRequiredUIConfiguration.class,
        MockPropertiesConfiguration.class, MockServerConfiguration.class, ControllerServerManager.class, ControllingService.class,
        ExceptionLoggingService.class, ActionProcessor.class, JNAMouseMovingService.class, SettingsComponent.class })
public class TestConfiguration {

}
