package com.github.lulewiczg.controller;

import java.nio.charset.Charset;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.Settings;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Runs server program.
 *
 * @author Grzegurz
 */
public class Main {

    private static final String CONSOLE = "console";

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
        Appender consoleAppender = ConsoleAppender.createDefaultAppenderForLayout(layout);
        consoleAppender.start();
        AppenderRef ref = AppenderRef.createAppenderRef("CONSOLE_APPENDER", null, null);
        AppenderRef[] refs = new AppenderRef[] { ref };
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, "CONSOLE_LOGGER", "", refs, null, config, null);
        loggerConfig.addAppender(consoleAppender, null, null);
        if (window) {
            JTextAreaAppender windowAppender = JTextAreaAppender.createAppender("SWING_APPENDER", 0, false, layout, null);
            windowAppender.start();
            loggerConfig.addAppender(windowAppender, null, null);
            config.addAppender(windowAppender);
        }
        config.addAppender(consoleAppender);
        config.addLogger("", loggerConfig);
        ctx.updateLoggers(config);
    }

    /**
     * Runs server either in windowed or in console mode.
     *
     * @param args
     */
    public static void main(String... args) {
        Settings settings = Settings.loadSettigs();
        if (args.length >= 1) {
            configureLogger(false);
            if (args[0].equals(CONSOLE)) {
                ControllerServer server = ControllerServer.getInstance();
                if (args.length == 2) {
                    settings.setPort(Integer.parseInt(args[1]));
                }
                server.start(new Settings(settings.getPort(), settings.getPassword(), true, false, true));
            }
        } else {
            configureLogger(true);
            new ServerWindow();
        }
    }
}
