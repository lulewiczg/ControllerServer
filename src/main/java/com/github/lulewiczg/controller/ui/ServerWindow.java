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

import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.Settings;

/**
 * GUI for server.
 *
 * @author Grzegurz
 */
public class ServerWindow extends JFrame {

    private static final String INVALID_PASSWORD = "Invalid password!";
    private static final String INVALID_PORT = "Invalid port!";
    private static final String CONTROLLER_SERVER = "Controller server";
    private static final Logger log = LogManager.getLogger(ServerWindow.class);
    private static final int SLEEP = 1000;
    private static final long serialVersionUID = 2687314377956367316L;
    private ControllerServer server;
    private JButton stop;
    private JButton start;
    private JLabel stateIndicator;
    private Thread monitorThread;
    private JTextField portInput;
    private JComboBox<Level> levels;
    private LoggerConfig loggerConfig;
    private LoggerContext ctx;
    private Settings settings;
    private JCheckBox autostart;
    private JCheckBox restart;
    private JTextField passwordInput;

    public ServerWindow() {
        settings = Settings.getSettings();
        ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        server = ControllerServer.getInstance();
        setTitle(CONTROLLER_SERVER);
        setSize(400, 600);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.catching(e);
        }
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
        if (settings.isAutostart()) {
            server.start(settings);
        }
    }

    /**
     * Saves settings and quits.
     */
    private void quit() {
        Settings.saveSettings();
        System.exit(0);
    }

    /**
     * Inits UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(createSettingsPanel(), BorderLayout.NORTH);
        add(createLogPanel());
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
                    log.catching(e);
                }
            }
        });
        monitorThread.start();
    }

    /**
     * Creates logs panel.
     *
     * @return log panel
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Logs"));
        Level[] values = Level.values();
        Arrays.sort(values);
        levels = new JComboBox<>(values);
        levels.addActionListener(buildListener(e -> {
            Level level = (Level) levels.getSelectedItem();
            settings.setLevel(level);
            loggerConfig.setLevel(Level.INFO);
            ctx.updateLoggers();
            log.info("Logger level changed to: " + level);
            loggerConfig.setLevel(level);
            ctx.updateLoggers();
        }));
        levels.setSelectedItem(settings.getLevel());
        levels.setMaximumSize(new Dimension(50, 20));
        panel.add(levels, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Arial", 0, 11));
        JScrollPane scrollPanel = new JScrollPane(textArea);
        JTextAreaAppender.addTextArea(textArea);
        panel.add(scrollPanel);

        return panel;
    }

    /**
     * Creates settings panel.
     *
     * @return setting panel
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 3));
        panel.setBorder(BorderFactory.createTitledBorder("Server settings"));
        JLabel ip = new JLabel("IP");
        String[] localIps = getIPs();
        JComboBox<String> ipInput = new JComboBox<>(localIps);
        ipInput.setEditable(false);
        panel.add(ip);
        panel.add(ipInput);

        JLabel port = new JLabel("Port");
        portInput = new JTextField(String.valueOf(settings.getPort()));
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
        panel.add(port);
        panel.add(portInput);

        JLabel password = new JLabel("Password");
        passwordInput = new JTextField(String.valueOf(settings.getPassword()));
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
        panel.add(password);
        panel.add(passwordInput);

        autostart = new JCheckBox("Auto start server on startup", settings.isAutostart());
        autostart.addActionListener(buildListener(e -> settings.setAutostart(autostart.isSelected())));
        panel.add(autostart);
        restart = new JCheckBox("Restart on error", settings.isRestartOnError());
        restart.addActionListener(buildListener(e -> server.start(settings)));
        panel.add(restart);

        JLabel state = new JLabel("Server state");
        stateIndicator = new JLabel();
        Font font = stateIndicator.getFont();
        stateIndicator.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
        panel.add(state);
        panel.add(stateIndicator);

        stop = new JButton("Stop");
        stop.addActionListener(buildListener(e -> server.stop()));
        start = new JButton("Start");
        start.addActionListener(buildListener(e -> server.start(settings)));
        panel.add(stop);
        panel.add(start);
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
        start.setEnabled(!enabled);
        portInput.setEnabled(!enabled);
        passwordInput.setEnabled(!enabled);
        stop.setEnabled(enabled);
    }

    /**
     * Shows error when value is invalid.
     *
     * @param message
     *            message
     */
    private void invalidValue(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        start.setEnabled(false);
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
