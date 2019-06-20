package com.github.lulewiczg.controller;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.server.ControllerServer;

@Profile("test")
@Configuration
@ImportAutoConfiguration(value = { ControllerServer.class, ControllingService.class })
public class TestConfiguration {

}
