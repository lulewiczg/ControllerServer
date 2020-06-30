package com.github.lulewiczg.controller.ui;

import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

/**
 * GUI for server.
 *
 * @author Grzegurz
 */
@Lazy
@Log4j2
@Component
public class ServerWindow extends JFrame {
    private static final String CONTROLLER_SERVER = "Controller server";
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient ExceptionLoggingService exceptionService;

    @Autowired
    private transient JTextAreaAppender appender;

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
    private JTextField timeoutInput;

    @Autowired
    private JComboBox<UIConfiguration.ComboboxEntry> connectionTypeCombobox;

    @Autowired
    private JPanel settingsPanel;

    @Autowired
    private JPanel logPanel;

    @Autowired
    private transient ServerWindowAdapter adapter;

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
        addWindowListener(adapter);
        revalidate();
        appender.flush();
    }

    /**
     * Inits UI.
     */
    private void initUI() {
        setTitle(CONTROLLER_SERVER);
        setSize(400, 600);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        setLayout(new BorderLayout());
        add(settingsPanel, BorderLayout.NORTH);
        add(logPanel);
        setLocationRelativeTo(null);
    }

    /**
     * Updates components depending on server state.
     *
     * @param state server state
     */
    public void updateUI(ServerState state) {
        SwingUtilities.invokeLater(() -> {
            boolean running = state.isRunning();
            portInput.setEnabled(!running);
            passwordInput.setEnabled(!running);
            timeoutInput.setEnabled(!running);
            connectionTypeCombobox.setEnabled(!running);
            startButton.setEnabled(!running);
            stopButton.setEnabled(running);
            stateIndicator.setText(state.getMsg());
            revalidate();
        });
    }

}
