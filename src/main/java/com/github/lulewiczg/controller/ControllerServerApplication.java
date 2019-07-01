package com.github.lulewiczg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Lazy;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Runs server program.
 *
 * @author Grzegurz
 */
@SpringBootApplication
public class ControllerServerApplication implements CommandLineRunner {

    private static final String CONSOLE = "console";

    @Lazy
    @Autowired
    private ServerWindow window;

    @Autowired
    private ControllerServerManager manager;

    /**
     * Runs server either in windowed or in console mode.
     *
     * @param args
     */
    public static void main(String... args) {
        new SpringApplicationBuilder(ControllerServerApplication.class).headless(false).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1 || !args[0].equals(CONSOLE)) {
            window.startUI();
        }
        while (manager.getStatus() != ServerState.FORCED_SHUTDOWN) {
            Thread.sleep(2000);
        }
        System.exit(0);
    }

}
