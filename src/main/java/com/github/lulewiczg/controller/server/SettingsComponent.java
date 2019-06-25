package com.github.lulewiczg.controller.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;

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

    @Value("${com.github.lulewiczg.setting.password}")
    private String password;

    @Value("${com.github.lulewiczg.setting.port}")
    private int port;

    @Value("${com.github.lulewiczg.setting.autostart}")
    private boolean autostart;

    @Value("${logging.level.com.github.lulewiczg}")
    private Level logLevel;

    @Value("${com.github.lulewiczg.setting.userFile}")
    private String propsFile;

    @Resource(name = "userProperties")
    private Properties userProperties;

    private ExceptionLoggingService loggingService;

    @Autowired
    public SettingsComponent(ExceptionLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Saves settings.
     */
    public void saveSettings() {
        if (userProperties.isEmpty()) {
            return;
        }
        File f = new File(propsFile);
        try {
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            try (out) {
                userProperties.store(out, "");
            }
            log.debug("Settings saved");
        } catch (IOException e) {
            loggingService.error(log, "Could not save settings file", e);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        userProperties.setProperty("com.github.lulewiczg.setting.password", password);
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        userProperties.setProperty("com.github.lulewiczg.setting.port", port + "");
        this.port = port;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        userProperties.setProperty("com.github.lulewiczg.setting.autostart", autostart + "");
        this.autostart = autostart;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        userProperties.setProperty("logging.level.com.github.lulewiczg", logLevel.toString());
        this.logLevel = logLevel;
    }

}
