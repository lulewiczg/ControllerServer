package com.github.lulewiczg.controller.server;

import java.io.Serializable;
import java.util.Random;

import org.apache.logging.log4j.Level;

/**
 * Holds server settings.
 *
 * @author Grzegurz
 */
public class Settings implements Serializable {

    private static final long serialVersionUID = 7010493017533238086L;
    private static final String PASSWORD_FORMAT = "%03d";
    private static final int PASSWORD_MAX = 999999;

    private int port = 5555;
    private String password;
    private Level level = Level.ERROR;
    private boolean autostart = false;

    /**
     * Generates password.
     */
    private void genPassword() {
        Random r = new Random();
        int p = r.nextInt(PASSWORD_MAX);
        password = String.format(PASSWORD_FORMAT, p);
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

}
