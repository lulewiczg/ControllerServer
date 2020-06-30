package com.github.lulewiczg.controller.ui;

import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 * Displays popups using swing.
 *
 * @author Grzegurz
 */
@Component
public class SwingPopup {

    private static final String CONTROLLER_SERVER = "Controller server";

    /**
     * Shows error when value is invalid and disables given button.
     *
     * @param message message
     * @param button  button to disable
     */
    public void invalidValuePopup(String message, JButton button) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        button.setEnabled(false);
    }

    /**
     * Shows confirm message when exiting app.
     *
     * @return selected option
     */
    public int showExitConfirm() {
        String[] objButtons = {"Yes", "No"};
        return JOptionPane.showOptionDialog(null, "Server is still running, are you sure you want to exit?", CONTROLLER_SERVER,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, objButtons, objButtons[1]);
    }
}
