package com.github.lulewiczg.controller;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import javax.swing.JTextArea;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.SettingsComponent;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.ServerWindow;

/**
 * Runs server program.
 *
 * @author Grzegurz
 */
@SpringBootApplication
public class ControllerServerApplication implements CommandLineRunner {

    private static final String CONSOLE = "console";

    @Autowired
    private ControllerServerManager server;

    @Autowired
    private ServerWindow window;

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private JTextAreaAppender appender;

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
        if (args.length >= 1) {
            if (args[0].equals(CONSOLE)) {
                if (args.length == 2) {
                    settings.setPort(Integer.parseInt(args[1]));
                }
                server.start();
            }
        } else {
            appender.setEnableOutput(true);
            window.run();
        }
    }

}
