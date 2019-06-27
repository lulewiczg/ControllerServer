package com.github.lulewiczg.controller.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * GUI for server.
 *
 * @author Grzegurz
 */
@Lazy
@Component
public class ServerWindow extends JFrame {
    private static final Logger log = LogManager.getLogger(ServerWindow.class);
    private static final String CONTROLLER_SERVER = "Controller server";
    private static final long serialVersionUID = 1L;

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private ControllerServerManager server;

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private JTextAreaAppender appender;

    @Autowired
    private JButton stopButton;

    @Autowired
    private JButton startButton;

    @Autowired
    private JLabel stateIndicator;

    @Autowired
    private JTextField portInput;

    @Autowired
    private JTextField passwordInput;

    @Autowired
    private JPanel settingsPanel;

    @Autowired
    private JPanel logPanel;

    public ServerWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            exceptionService.error(log, e);
        }
    }

    /**
     * Starts window UI.
     */
    public void startUI() {
        appender.setEnableOutput(true);
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (server.isRunning()) {
                    String ObjButtons[] = { "Yes", "No" };
                    int PromptResult = JOptionPane.showOptionDialog(null,
                            "Server is still running, are you sure you want to exit?", CONTROLLER_SERVER,
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]);
                    if (PromptResult == JOptionPane.YES_OPTION) {
                        quit();
                    }
                } else {
                    quit();
                }
            }
        });
        revalidate();
        appender.flush();
    }

    /**
     * Inits UI.
     */
    private void initUI() {
        setTitle(CONTROLLER_SERVER);
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        setLayout(new BorderLayout());
        add(settingsPanel, BorderLayout.NORTH);
        add(logPanel);
        setLocationRelativeTo(null);
    }

    /**
     * Saves settings and quits.
     */
    private void quit() {
        settings.saveSettings();
        System.exit(0);
    }

    /**
     * Updates components depending on server state.
     *
     * @param enabled
     *            enabled
     */
    public void updateUI(ServerState state) {
        SwingUtilities.invokeLater(() -> {
            boolean shutdown = state == ServerState.SHUTDOWN;
            portInput.setEnabled(shutdown);
            passwordInput.setEnabled(shutdown);
            startButton.setEnabled(shutdown);
            stopButton.setEnabled(!shutdown);
            stateIndicator.setText(state.getMsg());
            revalidate();
        });
    }

}
