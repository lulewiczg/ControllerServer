package com.github.lulewiczg.controller.ui;

import static org.junit.Assert.assertThat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Tests application UI.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { AWTTestConfiguration.class, EagerConfiguration.class, UIConfiguration.class, ServerWindow.class,
        ServerWindowAdapter.class })
@EnableAutoConfiguration
public class ServerWindowTest {

    private static final String TEST = "test";

    @MockBean
    private ControllerServerManager manager;

    @MockBean
    private Properties properties;

    @MockBean
    private ExceptionLoggingService exceptionService;

    @MockBean
    private SettingsComponent settings;

    @MockBean
    private JTextAreaAppender appender;

    @MockBean
    private SwingPopup popup;

    @MockBean
    private ServerWindowAdapter adapter;

    @SpyBean
    private ServerWindow window;

    @Autowired
    private JPanel logPanel;

    @Autowired
    private JPanel settingsPanel;

    @Autowired
    private JButton clearLogsButton;

    @Autowired
    private JComboBox<Level> logLevelsCombobox;

    @Autowired
    private JComboBox<String> ipCombobox;

    @Autowired
    private JTextArea textArea;

    @Autowired
    private JTextField portInput;

    @Autowired
    private JTextField passwordInput;

    @Autowired
    private JCheckBox autostart;

    @Autowired
    private JLabel stateIndicator;

    @Autowired
    private JButton startButton;

    @Autowired
    private JButton stopButton;

    @BeforeEach
    public void before() {
        Mockito.when(settings.isAutostart()).thenReturn(true);
        Mockito.when(settings.getPassword()).thenReturn("password");
        Mockito.when(settings.getPort()).thenReturn(151515);
        Mockito.when(settings.getLogLevel()).thenReturn(Level.FATAL);
        startButton.setEnabled(true);
    }

    @AfterEach
    public void after() {
        window.setVisible(false);
        window.dispose();
    }

    @Test
    @DisplayName("Logs panel is created properly")
    public void testLogsPanel() throws Exception {
        assertThat(logPanel.getComponentCount(), Matchers.equalTo(2));

        JPanel panel = (JPanel) logPanel.getComponent(0);
        assertThat(panel.getComponentCount(), Matchers.equalTo(2));
        assertThat(panel.getComponent(0), Matchers.equalTo(clearLogsButton));
        assertThat(panel.getComponent(1), Matchers.equalTo(logLevelsCombobox));

        JScrollPane panel2 = (JScrollPane) logPanel.getComponent(1);
        assertThat(panel2.getComponentCount(), Matchers.greaterThanOrEqualTo(1));
        JViewport view = (JViewport) panel2.getComponent(0);
        assertThat(view.getComponentCount(), Matchers.equalTo(1));
        assertThat(view.getComponent(0), Matchers.equalTo(textArea));
    }

    @Test
    @DisplayName("Settings panel is creating properly")
    public void testSettingsPanel() throws Exception {
        assertThat(settingsPanel.getComponentCount(), Matchers.equalTo(12));

        JLabel label = (JLabel) settingsPanel.getComponent(0);
        assertThat(label.getText(), Matchers.equalTo("IP"));
        assertThat(settingsPanel.getComponent(1), Matchers.equalTo(ipCombobox));

        JLabel label2 = (JLabel) settingsPanel.getComponent(2);
        assertThat(label2.getText(), Matchers.equalTo("Port"));
        assertThat(settingsPanel.getComponent(3), Matchers.equalTo(portInput));

        JLabel label3 = (JLabel) settingsPanel.getComponent(4);
        assertThat(label3.getText(), Matchers.equalTo("Password"));
        assertThat(settingsPanel.getComponent(5), Matchers.equalTo(passwordInput));

        assertThat(settingsPanel.getComponent(6), Matchers.equalTo(autostart));

        JLabel label4 = (JLabel) settingsPanel.getComponent(8);
        assertThat(label4.getText(), Matchers.equalTo("Server state"));
        assertThat(settingsPanel.getComponent(9), Matchers.equalTo(stateIndicator));

        assertThat(settingsPanel.getComponent(10), Matchers.equalTo(stopButton));
        assertThat(settingsPanel.getComponent(11), Matchers.equalTo(startButton));
    }

    @Test
    @DisplayName("Clear logs button")
    public void testClearLogsButton() throws Exception {
        assertThat(clearLogsButton.getText(), Matchers.equalTo("Clear logs"));
        textArea.setText("test text");
        clearLogsButton.doClick();
        assertThat(textArea.getText(), Matchers.equalTo(""));
    }

    @Test
    @DisplayName("IP combobox")
    public void testIpCombobox() throws Exception {
        assertThat(ipCombobox.isEditable(), Matchers.equalTo(false));
        InetAddress[] ips = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
        List<String> localIps = Arrays.stream(ips).map(InetAddress::getHostAddress).sorted().collect(Collectors.toList());
        assertThat(ipCombobox.getModel().getSize(), Matchers.equalTo(localIps.size()));
        IntStream.range(0, localIps.size())
                .forEach(i -> assertThat(ipCombobox.getModel().getElementAt(i), Matchers.equalTo(localIps.get(i))));
    }

    @Test
    @DisplayName("Stop button")
    public void testStopButton() {
        assertThat(stopButton.getText(), Matchers.equalTo("Stop"));
        stopButton.doClick();
        Mockito.verify(manager).stop();
    }

    @Test
    @DisplayName("Start button")
    public void testStartButton() {
        assertThat(startButton.getText(), Matchers.equalTo("Start"));
        startButton.doClick();
        Mockito.verify(manager).start();
    }

    @Test
    @DisplayName("Autostart checkbox")
    public void testAutostartCheckbox() {
        assertThat(autostart.getText(), Matchers.equalTo("Auto start server on startup"));
        boolean selected = autostart.isSelected();

        autostart.doClick();
        selected ^= true;
        Mockito.verify(settings).setAutostart(selected);

        autostart.doClick();
        selected ^= true;
        Mockito.verify(settings).setAutostart(selected);
    }

    @Test
    @DisplayName("Port input")
    public void testPortInput() {
        portInput.setText("12345");
        portInput.dispatchEvent(new ActionEvent(portInput, 1, TEST));
        portInput.postActionEvent();

        Mockito.verify(popup, Mockito.never()).invalidValuePopup(Mockito.anyString(), Mockito.any());
        Mockito.verify(settings).setPort(12345);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Port input - invalid port")
    public void testPortInputInvalidPort() {
        portInput.setText("qwertyu");
        portInput.dispatchEvent(new ActionEvent(portInput, 1, TEST));
        portInput.postActionEvent();

        Mockito.verify(popup).invalidValuePopup("Invalid port!", startButton);
        Mockito.verify(settings, Mockito.never()).setPort(Mockito.anyInt());
    }

    @Test
    @DisplayName("Port input - empty")
    public void testPortInputEmpty() {
        portInput.setText("");
        portInput.dispatchEvent(new ActionEvent(portInput, 1, TEST));
        portInput.postActionEvent();

        Mockito.verify(popup).invalidValuePopup("Invalid port!", startButton);
        Mockito.verify(settings, Mockito.never()).setPort(Mockito.anyInt());
    }

    @Test
    @DisplayName("Password input")
    public void testPasswordInput() {
        passwordInput.setText(TEST);
        passwordInput.dispatchEvent(new ActionEvent(passwordInput, 1, TEST));
        passwordInput.postActionEvent();

        Mockito.verify(popup, Mockito.never()).invalidValuePopup(Mockito.anyString(), Mockito.any());
        Mockito.verify(settings).setPassword(TEST);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Password input - empty")
    public void testPasswordInputEmpty() {
        passwordInput.setText("");
        passwordInput.dispatchEvent(new ActionEvent(passwordInput, 1, TEST));
        passwordInput.postActionEvent();

        Mockito.verify(popup).invalidValuePopup("Invalid password!", startButton);
        Mockito.verify(settings, Mockito.never()).setPassword(Mockito.anyString());
    }

    @Test
    @DisplayName("Logs levels combobox")
    public void testLogLevelsCombobox() {
        Level[] levels = Level.values();
        Arrays.sort(levels);
        assertThat(logLevelsCombobox.getModel().getSize(), Matchers.equalTo(levels.length));
        IntStream.range(0, levels.length)
                .forEach(i -> assertThat(logLevelsCombobox.getModel().getElementAt(i), Matchers.equalTo(levels[i])));

        logLevelsCombobox.setSelectedItem(Level.WARN);
        Mockito.verify(settings).setLogLevel(Level.WARN);
        Mockito.verify(appender).updateFilter(Level.WARN);
    }

    @Test
    @DisplayName("Update server status - SHUTDOWN")
    public void testUpdateServerStateShutdown() throws InterruptedException {
        window.updateUI(ServerState.SHUTDOWN);
        verifyNotChangedComponents();

        Thread.sleep(500);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(false));
        assertThat(portInput.isEnabled(), Matchers.equalTo(true));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Update server status - WAITING")
    public void testUpdateServerStateWaiting() throws InterruptedException {
        window.updateUI(ServerState.WAITING);
        verifyNotChangedComponents();

        Thread.sleep(500);
        assertThat(startButton.isEnabled(), Matchers.equalTo(false));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(true));
        assertThat(portInput.isEnabled(), Matchers.equalTo(false));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Update server status - CONNECTED")
    public void testUpdateServerStateConnected() throws InterruptedException {
        window.updateUI(ServerState.CONNECTED);
        verifyNotChangedComponents();

        Thread.sleep(500);
        assertThat(startButton.isEnabled(), Matchers.equalTo(false));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(true));
        assertThat(portInput.isEnabled(), Matchers.equalTo(false));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("UI configuration")
    public void testUIConfig() throws InterruptedException {
        window.startUI();

        Mockito.verify(appender).setEnableOutput(true);
        Mockito.verify(window).setTitle("Controller server");
        Mockito.verify(window).setSize(400, 600);
        Mockito.verify(window).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Mockito.verify(window).setVisible(true);
        Mockito.verify(window).setLayout(Mockito.any(BorderLayout.class));
        Mockito.verify(window).add(settingsPanel, BorderLayout.NORTH);
        Mockito.verify(window).add(logPanel);
        Mockito.verify(window).setLocationRelativeTo(null);
        Mockito.verify(window).addWindowListener(Mockito.any(ServerWindowAdapter.class));

        Mockito.verify(window).revalidate();
        Mockito.verify(appender).flush();
    }

    /**
     * Verifies components not affected by server state.
     */
    private void verifyNotChangedComponents() {
        assertThat(autostart.isEnabled(), Matchers.equalTo(true));
        assertThat(ipCombobox.isEnabled(), Matchers.equalTo(true));
        assertThat(logLevelsCombobox.isEnabled(), Matchers.equalTo(true));
    }
}
