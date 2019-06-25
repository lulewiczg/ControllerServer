package com.github.lulewiczg.controller.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for holding settings.
 *
 * @author Grzegurz
 */
@Component
public class SettingsComponent {

    private static final Logger log = LogManager.getLogger(SettingsComponent.class);

    private String password;

    private int port;

    private boolean autostart;

    private Level logLevel;

    private Properties properties = new Properties();

    private ExceptionLoggingService loggingService;

    @Autowired
    public SettingsComponent(ExceptionLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Saves settings.
     */
    public void saveSettings() {
        File f = new File("application.properties");
        try {
            f.createNewFile();
            properties.store(new FileOutputStream(f), "");
            log.debug("Settings saved");
        } catch (IOException e) {
            loggingService.error(log, "Could not save settings file", e);
        }
    }

    public String getPassword() {
        return password;
    }

    @Value("${com.github.lulewiczg.setting.password}")
    public void setPassword(String password) {
        properties.setProperty("com.github.lulewiczg.setting.password", password);
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    @Value("${com.github.lulewiczg.setting.port}")
    public void setPort(int port) {
        properties.setProperty("com.github.lulewiczg.setting.port", port + "");
        this.port = port;
    }

    public boolean isAutostart() {
        return autostart;
    }

    @Value("${com.github.lulewiczg.setting.autostart}")
    public void setAutostart(boolean autostart) {
        properties.setProperty("com.github.lulewiczg.setting.autostart", autostart + "");
        this.autostart = autostart;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    @Value("${logging.level.com.github.lulewiczg}")
    public void setLogLevel(Level logLevel) {
        properties.setProperty("logging.level.com.github.lulewiczg", logLevel.toString());
        this.logLevel = logLevel;
    }

}
