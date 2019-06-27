package com.github.lulewiczg.controller;

import java.util.Properties;

import javax.swing.JTextArea;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

@Profile("test")
@Configuration
@ImportAutoConfiguration(value = { ExceptionLoggingService.class, SettingsComponent.class })
public class LightTestConfiguration {

    @MockBean
    private JTextArea textArea;

    @Bean
    public Properties userProperties() {
        return new Properties();
    };

}
