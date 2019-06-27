package com.github.lulewiczg.controller.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

/**
 * Displays popups using swing.
 * 
 * @author Grzegurz
 */
@Component
public class SwingPopup {

    /**
     * Shows error when value is invalid and disables given button.
     *
     * @param message
     *            message
     * @param button
     *            button to disable
     */
    public void invalidValuePopup(String message, JButton button) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        button.setEnabled(false);
    }
}
