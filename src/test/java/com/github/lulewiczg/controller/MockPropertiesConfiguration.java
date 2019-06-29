package com.github.lulewiczg.controller;

import java.util.Properties;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for reading test properties
 *
 * @author Grzegurz
 */
@Configuration
public class MockPropertiesConfiguration {

    @MockBean(name = "userProperties")
    private Properties userProperties;

}
