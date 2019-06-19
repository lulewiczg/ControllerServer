package com.github.lulewiczg.controller.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.common.SerializerType;

/**
 * Holds server settings.
 *
 * @author Grzegurz
 */
public class Settings implements Serializable {

    private static final String SETTINGS_DAT = "settings.dat";
    private static final long serialVersionUID = 7010493017533238086L;
    private static final String PASSWORD_FORMAT = "%03d";
    private static final int PASSWORD_MAX = 999999;
    public static final Logger log = LogManager.getLogger(Settings.class);
    private static Settings settings;

    private int port = 5555;
    private String password;
    private Level level = Level.ERROR;
    private boolean autostart = false;
    private boolean restartOnError = false;
    private SerializerType serialier = SerializerType.OBJECT_STREAM;

    /**
     * Generates password.
     */
    private void genPassword() {
        Random r = new Random();
        int p = r.nextInt(PASSWORD_MAX);
        password = String.format(PASSWORD_FORMAT, p);
    }

    /**
     * Loads saved settings.
     *
     * @return settings
     */
    public static Settings loadSettigs() {
        Settings settings;
        try (FileInputStream fin = new FileInputStream(SETTINGS_DAT); ObjectInputStream ois = new ObjectInputStream(fin);) {
            settings = (Settings) ois.readObject();
        } catch (FileNotFoundException e) {
            log.catching(e);
            log.info("Settings not found, creating new...");
            settings = new Settings();
            saveSettings(settings);
        } catch (Exception e) {
            log.error("Could not load settings file");
            log.catching(e);
            settings = new Settings();
        }
        Settings.settings = settings;
        return settings;
    }

    /**
     * Saves settings.
     */
    public static void saveSettings() {
        saveSettings(settings);
    }

    /**
     * Saves settings
     *
     * @param settings
     *            settings
     */
    public static void saveSettings(Settings settings) {
        try (FileOutputStream fos = new FileOutputStream(new File(SETTINGS_DAT));
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(settings);
        } catch (IOException e) {
            log.error("Error while saving file");
            log.catching(e);
        }
    }

    public static Settings getSettings() {
        return settings;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public boolean isRestartOnError() {
        return restartOnError;
    }

    public void setRestartOnError(boolean restartOnError) {
        this.restartOnError = restartOnError;
    }

    public String getPassword() {
        if (password == null) {
            genPassword();
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public Level getLevel() {
        return level;
    }

    public Settings() {
    }

    public Settings(int port, String password, boolean autostart, boolean restartOnError) {
        this.port = port;
        this.password = password;
        this.autostart = autostart;
        this.restartOnError = restartOnError;
    }

    public SerializerType getSerialier() {
        return serialier;
    }

    public void setSerialier(SerializerType serialier) {
        this.serialier = serialier;
    }

}
