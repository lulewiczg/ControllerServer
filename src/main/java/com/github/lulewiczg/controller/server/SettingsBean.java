package com.github.lulewiczg.controller.server;

import org.springframework.stereotype.Component;

/**
 * Component for holding settings
 *
 * @author Grzegurz
 */
@Component
public class SettingsBean {
    private Settings settings;

    public SettingsBean() {
        settings = Settings.loadSettings();
    }

    public Settings getSettings() {
        return settings;
    }

}
