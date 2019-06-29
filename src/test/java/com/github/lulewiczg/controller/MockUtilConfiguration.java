package com.github.lulewiczg.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Configuration for common classes, mocks unnecessary classes.
 *
 * @author Grzegurz
 */
@Configuration
@Import({ ExceptionLoggingService.class, SettingsComponent.class })
public class MockUtilConfiguration {

    @MockBean
    private ControllingService service;
}
