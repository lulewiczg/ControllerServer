package com.github.lulewiczg.controller.server;

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

import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.ServerWindow;

public class Main {

    private static final String CONSOLE = "console";

    private static void configureLogger(boolean window) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("[%d{dd.MM.YY HH:mm:ss}] [%p] - %m%ex%n", null, null, null,
                Charset.defaultCharset(), false, false, null, null);
        Appender appender;
        if (window) {
            appender = JTextAreaAppender.createAppender("SWING_APPENDER", 0, false, layout, null);
        } else {
            appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
        }
        appender.start();
        AppenderRef ref = AppenderRef.createAppenderRef("CONSOLE_APPENDER", null, null);

        AppenderRef[] refs = new AppenderRef[] { ref };
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, "CONSOLE_LOGGER", "", refs, null, config,
                null);
        loggerConfig.addAppender(appender, null, null);

        config.addAppender(appender);
        config.addLogger("", loggerConfig);
        ctx.updateLoggers(config);
    }

    public static void main(String... args) {
        Settings settings = Settings.loadSettigs();
        if (args.length >= 1) {
            configureLogger(false);
            if (args[0].equals(CONSOLE)) {
                ControllerServer server = ControllerServer.getInstance();
                if (args.length == 2) {
                    settings.setPort(Integer.parseInt(args[1]));
                }
                server.start(settings.getPort(), settings.getPassword(), false);
            }
        } else {
            configureLogger(true);
            new ServerWindow();
        }
    }
}
