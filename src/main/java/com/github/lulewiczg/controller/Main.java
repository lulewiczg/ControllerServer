package com.github.lulewiczg.controller;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.Settings;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Runs server program.
 *
 * @author Grzegurz
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    private static final String CONSOLE = "console";

    @Autowired
    private ControllerServer server;

    @Autowired
    private ServerWindow window;

    @Bean
    public Robot robot() throws AWTException {
        return new Robot();
    }

    @Bean
    public Clipboard clipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Configures loggers.
     *
     * @param window
     *            is in window
     */
    private static void configureLogger(boolean window) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{dd.MM.YYYY HH:mm:ss}] [%p] - %m%ex%n")
                .withCharset(Charset.defaultCharset()).build();
        LoggerConfig loggerConfig = config.getRootLogger();
        if (window) {
            JTextAreaAppender windowAppender = JTextAreaAppender.createAppender("SWING_APPENDER", 0, false, layout, null);
            windowAppender.start();
            loggerConfig.addAppender(windowAppender, null, null);
            config.addAppender(windowAppender);
        }
        config.addLogger("", loggerConfig);
        ctx.updateLoggers(config);
    }

    /**
     * Runs server either in windowed or in console mode.
     *
     * @param args
     */
    public static void main(String... args) {
        new SpringApplicationBuilder(Main.class).headless(false).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        Settings settings = Settings.loadSettigs();
        if (args.length >= 1) {
            configureLogger(false);
            if (args[0].equals(CONSOLE)) {
                if (args.length == 2) {
                    settings.setPort(Integer.parseInt(args[1]));
                }
                server.start(new Settings(settings.getPort(), settings.getPassword(), true, false));
            }
        } else {
            configureLogger(true);
            window.run();
        }
    }
}
