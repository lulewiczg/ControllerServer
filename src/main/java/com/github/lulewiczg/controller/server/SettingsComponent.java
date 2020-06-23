package com.github.lulewiczg.controller.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Component for holding settings.
 *
 * @author Grzegurz
 */
@Log4j2
@Getter
@Component
public class SettingsComponent {

    @Value("${com.github.lulewiczg.setting.password}")
    private String password;

    @Value("${com.github.lulewiczg.setting.port}")
    private int port;

    @Value("${com.github.lulewiczg.setting.serverTimeout}")
    private int timeout;

    @Value("${com.github.lulewiczg.setting.autostart}")
    private boolean autostart;

    @Value("${com.github.lulewiczg.setting.logLevel}")
    private Level logLevel;

    @Value("${com.github.lulewiczg.setting.userFile}")
    private String propsFile;

    @Resource(name = "userProperties")
    private Properties userProperties;

    @Setter
    private ExceptionLoggingService loggingService;

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

    public void setPassword(String password) {
        userProperties.setProperty("com.github.lulewiczg.setting.password", password);
        this.password = password;
    }

    public void setPort(int port) {
        userProperties.setProperty("com.github.lulewiczg.setting.port", port + "");
        this.port = port;
    }

    public void setTimeout(int timeout) {
        userProperties.setProperty("com.github.lulewiczg.setting.serverTimeout", timeout + "");
        this.timeout = timeout;
    }

    public void setAutostart(boolean autostart) {
        userProperties.setProperty("com.github.lulewiczg.setting.autostart", autostart + "");
        this.autostart = autostart;
    }

    public void setLogLevel(Level logLevel) {
        userProperties.setProperty("com.github.lulewiczg.setting.logLevel", logLevel.toString());
        this.logLevel = logLevel;
    }

}
