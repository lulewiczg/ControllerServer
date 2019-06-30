package com.github.lulewiczg.controller.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Window adapter for handling window events.
 *
 * @author Grzegurz
 */
@Component
public class ServerWindowAdapter extends WindowAdapter {

    private static final String CONTROLLER_SERVER = "Controller server";

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private ControllerServerManager serverManager;

    @Override
    public void windowClosing(WindowEvent we) {
        if (serverManager.isRunning()) {
            String ObjButtons[] = { "Yes", "No" };
            int PromptResult = JOptionPane.showOptionDialog(null, "Server is still running, are you sure you want to exit?",
                    CONTROLLER_SERVER, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]);
            if (PromptResult == JOptionPane.YES_OPTION) {
                quit();
            }
        } else {
            quit();
        }
    }

    /**
     * Saves settings and quits.
     *
     */
    private void quit() {
        settings.saveSettings();
        System.exit(0);
    }
}
