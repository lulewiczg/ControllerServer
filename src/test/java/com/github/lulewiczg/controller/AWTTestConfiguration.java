package com.github.lulewiczg.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Configuration for enabling AWT in tests.
 *
 * @author Grzegurz
 */
@Configuration
@ContextConfiguration(loader = AWTSpringApplicationContextLoader.class)
public class AWTTestConfiguration {

    {
        // SpringBootContextLoader not working when all tests are run
        System.setProperty("java.awt.headless", "false");
    }
}
