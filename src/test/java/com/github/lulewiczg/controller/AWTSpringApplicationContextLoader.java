package com.github.lulewiczg.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootContextLoader;

/**
 * Context loader that disables headless mode.
 *
 * @author Grzegurz
 */
public class AWTSpringApplicationContextLoader extends SpringBootContextLoader {

    @Override
    protected SpringApplication getSpringApplication() {
        return new SpringApplicationBuilder().headless(false).build();
    }

}