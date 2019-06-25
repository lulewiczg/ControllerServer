package com.github.lulewiczg.controller;

import javax.swing.JTextArea;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.lulewiczg.controller.server.ExceptionLoggingService;

@Profile("test")
@Configuration
@ImportAutoConfiguration(value = { ExceptionLoggingService.class })
public class LightTestConfiguration {

    @MockBean
    private JTextArea textArea;

}
