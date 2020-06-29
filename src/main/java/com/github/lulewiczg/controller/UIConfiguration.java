package com.github.lulewiczg.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.SwingPopup;

import lombok.extern.log4j.Log4j2;

/**
 * Beans for UI.
 *
 * @author Grzegurz
 */
/**
 * @author Grzegorz
 *
 */
@Log4j2
@Configuration
public class UIConfiguration {
    private static final String INVALID_PASSWORD = "Invalid password!";
    private static final String INVALID_PORT = "Invalid port!";
    private static final String INVALID_TIMEOUT = "Invalid timeout!";

    @Autowired
    private ExceptionLoggingService exceptionService;

    @Autowired
    private ControllerServerManager server;

    @Bean
    public JTextField passwordInput(SettingsComponent settings, JButton startButton, SwingPopup popup) {
        JTextField passwordInput = new JTextField(String.valueOf(settings.getPassword()));
        passwordInput.addFocusListener(createListener(() -> {
            String text = passwordInput.getText();
            if (text.isEmpty()) {
                popup.invalidValuePopup(INVALID_PASSWORD, startButton);
            } else {
                settings.setPassword(text);
                startButton.setEnabled(true);
            }
        }));
        return passwordInput;
    }

    @Bean
    public JComboBox<Level> levelsCombobox(SettingsComponent settings, JTextAreaAppender appender) {
        Level[] values = Level.values();
        Arrays.sort(values);
        JComboBox<Level> levels = new JComboBox<>(values);
        levels.setSelectedItem(settings.getLogLevel());
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        levels.addActionListener(e -> {
            Level level = (Level) levels.getSelectedItem();
            settings.setLogLevel(level);
            appender.updateFilter(level);
            log.info("Logger level changed to: " + level);
            ctx.updateLoggers();
        });
        levels.setMaximumSize(new Dimension(50, 20));
        return levels;
    }

    @Bean
    public JTextField portInput(SettingsComponent settings, JButton startButton, SwingPopup popup) {
        JTextField portInput = new JTextField(String.valueOf(settings.getPort()));
        portInput.addFocusListener(createListener(() -> {
            String text = portInput.getText();
            if (!text.isEmpty()) {
                try {
                    settings.setPort(Integer.valueOf(text));
                    startButton.setEnabled(true);
                } catch (Exception ex) {
                    exceptionService.debug(log, ex);
                    popup.invalidValuePopup(INVALID_PORT, startButton);
                }
            } else {
                popup.invalidValuePopup(INVALID_PORT, startButton);
            }
        }));
        return portInput;
    }

    @Bean
    public JTextField timeoutInput(SettingsComponent settings, JButton startButton, SwingPopup popup) {
        JTextField timeoutInput = new JTextField(String.valueOf(settings.getTimeout()));
        timeoutInput.addFocusListener(createListener(() -> {
            String text = timeoutInput.getText();
            if (!text.isEmpty()) {
                try {
                    settings.setTimeout(Integer.valueOf(text));
                    startButton.setEnabled(true);
                } catch (Exception ex) {
                    exceptionService.debug(log, ex);
                    popup.invalidValuePopup(INVALID_TIMEOUT, startButton);
                }
            } else {
                popup.invalidValuePopup(INVALID_TIMEOUT, startButton);
            }
        }));
        return timeoutInput;
    }

    @Bean
    public JLabel stateIndicator() {
        JLabel stateIndicator = new JLabel();
        Font font = stateIndicator.getFont();
        stateIndicator.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
        return stateIndicator;
    }

    @Bean
    public JCheckBox autostartCheckbox(SettingsComponent settings) {
        JCheckBox autostart = new JCheckBox("Auto start server on startup", settings.isAutostart());
        autostart.setToolTipText("Start server automatically and wait for client after app start");
        autostart.addActionListener(e -> settings.setAutostart(autostart.isSelected()));
        return autostart;
    }

    @Bean
    public JButton startButton() {
        JButton start = new JButton("Start");
        start.addActionListener(e -> server.start());
        return start;
    }

    @Bean
    public JButton stopButton() {
        JButton stop = new JButton("Stop");
        stop.addActionListener(e -> server.stop());
        return stop;
    }

    @Bean
    public JComboBox<String> ipCombobox() {
        String[] localIps = getIPs();
        JComboBox<String> ipInput = new JComboBox<>(localIps);
        ipInput.setEditable(false);
        return ipInput;
    }

    @Bean
    public JButton clearLogsButton(JTextArea logsArea) {
        JButton clearLogsBtn = new JButton("Clear logs");
        clearLogsBtn.addActionListener(e -> logsArea.setText(""));
        return clearLogsBtn;
    }

    @Bean
    public JPanel settingsPanel(JComboBox<String> ipCombobox, JTextField portInput, JTextField passwordInput,
            JTextField timeoutInput, JCheckBox autostart, JLabel stateIndicator, JButton startButton, JButton stopButton) {
        JPanel panel = new JPanel(new GridLayout(7, 3));
        panel.setBorder(BorderFactory.createTitledBorder("Server settings"));
        JLabel ip = new JLabel("IP");
        ip.setToolTipText("Server IP, list contains all IPs that host PC has");
        panel.add(ip);
        panel.add(ipCombobox);

        JLabel port = new JLabel("Port");
        port.setToolTipText("Server port");
        panel.add(port);
        panel.add(portInput);

        JLabel password = new JLabel("Password");
        password.setToolTipText("Server passord");
        panel.add(password);
        panel.add(passwordInput);

        JLabel timeout = new JLabel("Timeout");
        timeout.setToolTipText("Timeout in miliseconds after idle");
        panel.add(timeout);
        panel.add(timeoutInput);

        panel.add(autostart);
        panel.add(new JLabel());

        JLabel state = new JLabel("Server state");
        state.setToolTipText("Current server state");
        panel.add(state);
        panel.add(stateIndicator);

        panel.add(stopButton);
        panel.add(startButton);
        return panel;
    }

    @Bean
    public JPanel logPanel(JComboBox<Level> levels, JTextArea logsArea, JButton clearLogsButton) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new GridLayout(1, 2));
        panel.add(buttons, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createTitledBorder("Logs"));
        buttons.add(clearLogsButton);

        buttons.add(levels);

        JScrollPane scrollPanel = new JScrollPane(logsArea);
        panel.add(scrollPanel);

        return panel;
    }

    /**
     * Gets computer IP addresses.
     *
     * @return IPs
     */
    private String[] getIPs() {
        String[] localIps = new String[] { "Unknown" };
        try {
            InetAddress[] ips = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            localIps = Arrays.stream(ips).map(InetAddress::getHostAddress).sorted().toArray(String[]::new);
        } catch (UnknownHostException e) {
            exceptionService.error(log, "Could not obtain computer address", e);
        }
        return localIps;
    }

    /**
     * Creates blur listener.
     *
     * @param lambda
     *            lambda to execute
     * @return listener
     */
    private FocusListener createListener(Runnable lambda) {
        return new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                lambda.run();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Do nothing
            }
        };
    }

}
