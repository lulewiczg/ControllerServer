package com.github.lulewiczg.controller.ui;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Window adapter for handling window events.
 *
 * @author Grzegurz
 */
@Component
public class ServerWindowAdapter extends WindowAdapter {

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private ControllerServerManager serverManager;

    @Autowired
    private SwingPopup popup;

    @Override
    public void windowClosing(WindowEvent we) {
        if (serverManager.isRunning()) {
            int promptResult = popup.showExitConfirm();
            if (promptResult == JOptionPane.YES_OPTION) {
                quit();
            }
        } else {
            quit();
        }
    }

    /**
     * Saves settings and quits.
     */
    private void quit() {
        settings.saveSettings();
        exit();
    }

    /**
     * Closes app.
     */
    void exit() {
        System.exit(0);
    }
}
