package com.github.lulewiczg.controller;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for reading test properties
 *
 * @author Grzegurz
 */
@Configuration
public class TestPropertiesConfiguration {
    @Bean
    public Properties userProperties() {
        return new Properties();
    }
}
