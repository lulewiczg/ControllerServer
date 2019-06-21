package com.github.lulewiczg.controller.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component for holding settings.
 *
 * @author Grzegurz
 */
@Component
public class SettingsComponent {

    private static final Logger log = LogManager.getLogger(SettingsComponent.class);
    private static final String SETTINGS_DAT = "settings.dat";

    @Autowired
    private ExceptionLoggingService loggingService;

    private Settings settings;

    public SettingsComponent() {
        settings = loadSettings();
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Loads saved settings.
     *
     * @return settings
     */
    private Settings loadSettings() {
        Settings settings;
        try (FileInputStream fin = new FileInputStream(SETTINGS_DAT); ObjectInputStream ois = new ObjectInputStream(fin);) {
            settings = (Settings) ois.readObject();
        } catch (FileNotFoundException e) {
            loggingService.log(log, "Settings not found, creating new...", e);
            settings = new Settings();
            saveSettings(settings);
        } catch (Exception e) {
            loggingService.log(log, "Could not load settings file", e);
            settings = new Settings();
        }
        return settings;
    }

    /**
     * Saves settings.
     *
     * @param settings
     *            settings
     */
    private void saveSettings(Settings settings) {
        try (FileOutputStream fos = new FileOutputStream(new File(SETTINGS_DAT));
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(settings);
        } catch (IOException e) {
            log.error("Error while saving file");
            log.catching(e);
        }
    }

    public void saveSettings() {
        saveSettings(settings);
    }

}
