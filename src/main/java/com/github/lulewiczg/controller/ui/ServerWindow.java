package com.github.lulewiczg.controller.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    private static final String INVALID_PASSWORD = "Invalid password!";
    private static final String INVALID_PORT = "Invalid port!";
    private static final String CONTROLLER_SERVER = "Controller server";
    private static final Logger log = LogManager.getLogger(ServerWindow.class);
    private static final int SLEEP = 1000;
    private static final long serialVersionUID = 2687314377956367316L;
    private LoggerContext ctx;
    private Thread monitorThread;
    private LoggerConfig loggerConfig;

    @Autowired
    private SettingsComponent settings;

    @Autowired
    private ControllerServerManager server;

    @Autowired
    private JTextArea logsArea;

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
    private JComboBox<Level> levels;

    @Autowired
    private JCheckBox autostart;

    @Autowired
    private JTextField passwordInput;

    @Autowired
    private JComboBox<String> ipCombobox;

    @Autowired
    private JPanel settingsPanel;

    @Autowired
    private JPanel logPanel;

    @Bean
    public JTextField passwordInput() {
        JTextField passwordInput = new JTextField(String.valueOf(settings.getPassword()));
        passwordInput.addActionListener(buildListener(e -> {
            String text = passwordInput.getText();
            if (!text.isEmpty()) {
                try {
                    settings.setPassword(text);
                } catch (Exception ex) {
                    invalidValue(INVALID_PASSWORD);
                }
            } else {
                invalidValue(INVALID_PASSWORD);
            }
        }));
        return passwordInput;
    }

    @Bean
    public JComboBox<Level> levelsCombobox() {
        Level[] values = Level.values();
        Arrays.sort(values);
        JComboBox<Level> levels = new JComboBox<>(values);
        levels.setSelectedItem(settings.getLogLevel());
        levels.addActionListener(buildListener(e -> {
            Level level = (Level) levels.getSelectedItem();
            settings.setLogLevel(level);
            loggerConfig.setLevel(Level.INFO);
            ctx.updateLoggers();
            log.info("Logger level changed to: " + level);
            loggerConfig.setLevel(level);
            ctx.updateLoggers();
        }));
        levels.setMaximumSize(new Dimension(50, 20));
        return levels;
    }

    @Bean
    public JTextField portInput() {
        JTextField portInput = new JTextField(String.valueOf(settings.getPort()));
        portInput.addActionListener(buildListener(e -> {
            String text = portInput.getText();
            if (!text.isEmpty()) {
                try {
                    settings.setPort(Integer.valueOf(text));
                } catch (Exception ex) {
                    invalidValue(INVALID_PORT);
                }
            } else {
                invalidValue(INVALID_PORT);
            }
        }));
        return portInput;
    }

    @Bean
    public JLabel stateIndicator() {
        JLabel stateIndicator = new JLabel();
        Font font = stateIndicator.getFont();
        stateIndicator.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
        return stateIndicator;
    }

    @Bean
    public JCheckBox autostartCheckbox() {
        JCheckBox autostart = new JCheckBox("Auto start server on startup", settings.isAutostart());
        autostart.addActionListener(buildListener(e -> settings.setAutostart(autostart.isSelected())));
        return autostart;
    }

    @Bean
    public JButton startButton() {
        JButton start = new JButton("Start");
        start.addActionListener(buildListener(e -> server.start()));
        return start;
    }

    @Bean
    public JButton stopButton() {
        JButton stop = new JButton("Stop");
        stop.addActionListener(buildListener(e -> server.stop()));
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
    public JPanel settingsPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 3));
        panel.setBorder(BorderFactory.createTitledBorder("Server settings"));
        JLabel ip = new JLabel("IP");
        panel.add(ip);
        panel.add(ipCombobox);

        JLabel port = new JLabel("Port");
        panel.add(port);
        panel.add(portInput);

        JLabel password = new JLabel("Password");
        panel.add(password);
        panel.add(passwordInput);

        panel.add(autostart);
        panel.add(new JLabel());

        JLabel state = new JLabel("Server state");
        panel.add(state);
        panel.add(stateIndicator);

        panel.add(stopButton);
        panel.add(startButton);
        return panel;
    }

    @Bean
    public JPanel logPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new GridLayout(1, 2));
        panel.add(buttons, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createTitledBorder("Logs"));
        JButton clearLogsBtn = new JButton("Clear logs");
        clearLogsBtn.addActionListener(buildListener(e -> logsArea.setText("")));
        buttons.add(clearLogsBtn);

        buttons.add(levels);

        JScrollPane scrollPanel = new JScrollPane(logsArea);
        panel.add(scrollPanel);

        return panel;
    }

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
        ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig("com.github.lulewiczg.controller");
        setTitle(CONTROLLER_SERVER);
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        initComponents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (server.getStatus() != ServerState.SHUTDOWN) {
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
     * Saves settings and quits.
     */
    private void quit() {
        settings.saveSettings();
        System.exit(0);
    }

    /**
     * Inits UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(settingsPanel, BorderLayout.NORTH);
        add(logPanel);
        monitorThread = new Thread(() -> {
            while (true) {
                ServerState s = server.getStatus();
                if (s == ServerState.SHUTDOWN) {
                    setServerRunnig(false);
                } else {
                    setServerRunnig(true);
                }
                stateIndicator.setText(s.getMsg());
                revalidate();
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    exceptionService.error(log, e);
                }
            }
        });
        monitorThread.start();
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
            e.printStackTrace();
        }
        return localIps;
    }

    /**
     * Updates components depending on server state.
     *
     * @param enabled
     *            enabled
     */
    private void setServerRunnig(boolean enabled) {
        startButton.setEnabled(!enabled);
        portInput.setEnabled(!enabled);
        passwordInput.setEnabled(!enabled);
        stopButton.setEnabled(enabled);
    }

    /**
     * Shows error when value is invalid.
     *
     * @param message
     *            message
     */
    private void invalidValue(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        startButton.setEnabled(false);
    }

    /**
     * Builds ActionListener from lambda.
     *
     * @param consumer
     *            lambda
     * @return action listenr
     */
    private ActionListener buildListener(Consumer<ActionEvent> consumer) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consumer.accept(e);
            }
        };
    }

}
