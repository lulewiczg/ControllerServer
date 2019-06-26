package com.github.lulewiczg.controller;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * Main application beans.
 *
 * @author Grzegurz
 */
@Configuration
public class MainConfiguration {

    @Bean
    public Robot robot() throws AWTException {
        return new Robot();
    }

    @Bean
    public Clipboard clipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    @Bean
    public PropertiesFactoryBean userProperties(@Value("${com.github.lulewiczg.setting.userFile}") String propsFile) {
        PropertiesFactoryBean res = new PropertiesFactoryBean();
        res.setFileEncoding("UTF-8");
        FileSystemResource location = new FileSystemResource(propsFile);
        if (location.exists()) {
            res.setLocation(location);
        }
        return res;
    }

}
