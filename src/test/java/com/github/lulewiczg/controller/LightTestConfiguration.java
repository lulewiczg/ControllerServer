package com.github.lulewiczg.controller;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

@Configuration
@ImportAutoConfiguration(value = { MockRequiredUIConfiguration.class, AWTTestConfiguration.class,
        TestPropertiesConfiguration.class, ExceptionLoggingService.class, SettingsComponent.class })
public class LightTestConfiguration {

}
