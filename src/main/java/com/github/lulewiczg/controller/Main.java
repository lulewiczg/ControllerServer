package com.github.lulewiczg.controller;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.SettingsBean;
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

    @Autowired
    private JTextArea loggingTextArea;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SettingsBean settings;

    @Value("${com.github.lulewiczg.logging.pattern}")
    private String logPattern;

    @Bean
    public Robot robot() throws AWTException {
        return new Robot();
    }

    @Bean
    public Clipboard clipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    @Bean
    public JTextArea logTextArea() {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setOpaque(false);
        jTextArea.setFont(new Font("Arial", 0, 11));
        return jTextArea;
    }

    /**
     * Configures loggers.
     *
     * @param window
     *            is in window
     */
    private void configureLogger(boolean window) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getRootLogger();
        if (window) {
            JTextAreaAppender windowAppender = context.getBean(JTextAreaAppender.class, loggingTextArea, logPattern);
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
        if (args.length >= 1) {
            configureLogger(false);
            if (args[0].equals(CONSOLE)) {
                if (args.length == 2) {
                    settings.getSettings().setPort(Integer.parseInt(args[1]));
                }
                server.start();
            }
        } else {
            configureLogger(true);
            window.run();
        }
    }
}
