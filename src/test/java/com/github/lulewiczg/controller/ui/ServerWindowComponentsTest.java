package com.github.lulewiczg.controller.ui;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests application UI.
 *
 * @author Grzegurz
 */
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(classes = { AWTTestConfiguration.class, EagerConfiguration.class, UIConfiguration.class, ServerWindow.class,
        ServerWindowAdapter.class })
@EnableAutoConfiguration
class ServerWindowComponentsTest {

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
    private JTextField timeoutInput;

    @Autowired
    private JCheckBox autostart;

    @Autowired
    private JButton startButton;

    @Autowired
    private JButton stopButton;

    private static JFrame frame = new JFrame();

    @BeforeEach
    void before() {
        when(settings.isAutostart()).thenReturn(true);
        when(settings.getPassword()).thenReturn("password");
        when(settings.getTimeout()).thenReturn(1122);
        when(settings.getPort()).thenReturn(151515);
        when(settings.getLogLevel()).thenReturn(Level.FATAL);
        startButton.setEnabled(true);
        if (frame.getComponentCount() == 1) {
            JPanel jPanel = new JPanel(new GridLayout(5, 5));
            jPanel.add(portInput);
            jPanel.add(passwordInput);
            jPanel.add(timeoutInput);
            frame.add(jPanel);
            frame.setSize(10, 10);
            frame.setVisible(true);
        }
    }

    @AfterEach
    void after() {
        window.setVisible(false);
        window.dispose();
    }

    @AfterAll
    static void afterAll() {
        frame.setVisible(false);
    }

    @Test
    @DisplayName("Clear logs button")
    void testClearLogsButton() {
        assertThat(clearLogsButton.getText(), Matchers.equalTo("Clear logs"));
        textArea.setText("test text");
        clearLogsButton.doClick();
        assertThat(textArea.getText(), Matchers.equalTo(""));
    }

    @Test
    @DisplayName("IP combobox")
    void testIpCombobox() throws Exception {
        assertThat(ipCombobox.isEditable(), Matchers.equalTo(false));
        InetAddress[] ips = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
        List<String> localIps = Arrays.stream(ips).map(InetAddress::getHostAddress).sorted().collect(Collectors.toList());
        assertThat(ipCombobox.getModel().getSize(), Matchers.equalTo(localIps.size()));
        IntStream.range(0, localIps.size())
                .forEach(i -> assertThat(ipCombobox.getModel().getElementAt(i), Matchers.equalTo(localIps.get(i))));
    }

    @Test
    @DisplayName("Stop button")
    void testStopButton() {
        assertThat(stopButton.getText(), Matchers.equalTo("Stop"));
        stopButton.doClick();
        verify(manager).stop();
    }

    @Test
    @DisplayName("Start button")
    void testStartButton() {
        assertThat(startButton.getText(), Matchers.equalTo("Start"));
        startButton.doClick();
        verify(manager).start();
    }

    @Test
    @DisplayName("Autostart checkbox")
    void testAutostartCheckbox() {
        assertThat(autostart.getText(), Matchers.equalTo("Auto start server on startup"));
        boolean selected = autostart.isSelected();

        autostart.doClick();
        selected ^= true;
        verify(settings).setAutostart(selected);

        autostart.doClick();
        selected ^= true;
        verify(settings).setAutostart(selected);
    }

    @Test
    @DisplayName("Port input")
    void testPortInput() throws InterruptedException {
        portInput.requestFocus();
        portInput.setText("12345");
        waitForAwt();
        portInput.dispatchEvent(new FocusEvent(portInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup, never()).invalidValuePopup(anyString(), any());
        verify(settings).setPort(12345);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Port input - invalid port")
    void testPortInputInvalidPort() throws InterruptedException {
        portInput.requestFocus();
        portInput.setText("qwertyu");
        waitForAwt();
        portInput.dispatchEvent(new FocusEvent(portInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup).invalidValuePopup("Invalid port!", startButton);
        verify(settings, never()).setPort(anyInt());
    }

    @Test
    @DisplayName("Port input - empty")
    void testPortInputEmpty() throws InterruptedException {
        portInput.requestFocus();
        portInput.setText("");
        waitForAwt();
        portInput.dispatchEvent(new FocusEvent(portInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup).invalidValuePopup("Invalid port!", startButton);
        verify(settings, never()).setPort(anyInt());
    }

    @Test
    @DisplayName("Password input")
    void testPasswordInput() throws InterruptedException {
        passwordInput.requestFocus();
        passwordInput.setText(TEST);
        waitForAwt();
        passwordInput.dispatchEvent(new FocusEvent(passwordInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup, never()).invalidValuePopup(anyString(), any());
        verify(settings).setPassword(TEST);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Password input - empty")
    void testPasswordInputEmpty() throws InterruptedException {
        passwordInput.requestFocus();
        passwordInput.setText("");
        waitForAwt();
        passwordInput.dispatchEvent(new FocusEvent(passwordInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup).invalidValuePopup("Invalid password!", startButton);
        verify(settings, never()).setPassword(anyString());
    }

    @Test
    @DisplayName("Timeout input")
    void testTimeoutInput() throws InterruptedException {
        timeoutInput.requestFocus();
        timeoutInput.setText("12345");
        waitForAwt();
        timeoutInput.dispatchEvent(new FocusEvent(timeoutInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup, never()).invalidValuePopup(anyString(), any());
        verify(settings).setTimeout(12345);
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Timeout input - empty")
    void testTimeoutInputEmpty() throws InterruptedException {
        timeoutInput.requestFocus();
        timeoutInput.setText("");
        waitForAwt();
        timeoutInput.dispatchEvent(new FocusEvent(timeoutInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup).invalidValuePopup("Invalid timeout!", startButton);
        verify(settings, never()).setTimeout(anyInt());
    }

    @Test
    @DisplayName("Timeout input - invalid port")
    void testTimeoutInputInvalidPort() throws InterruptedException {
        timeoutInput.requestFocus();
        timeoutInput.setText("qwertyu");
        waitForAwt();
        timeoutInput.dispatchEvent(new FocusEvent(passwordInput, FocusEvent.FOCUS_LOST));

        waitForAwt();
        verify(popup).invalidValuePopup("Invalid timeout!", startButton);
        verify(settings, never()).setTimeout(anyInt());
    }

    @Test
    @DisplayName("Logs levels combobox")
    void testLogLevelsCombobox() {
        Level[] levels = Level.values();
        Arrays.sort(levels);
        assertThat(logLevelsCombobox.getModel().getSize(), Matchers.equalTo(levels.length));
        IntStream.range(0, levels.length)
                .forEach(i -> assertThat(logLevelsCombobox.getModel().getElementAt(i), Matchers.equalTo(levels[i])));

        logLevelsCombobox.setSelectedItem(Level.WARN);
        verify(settings).setLogLevel(Level.WARN);
        verify(appender).updateFilter(Level.WARN);
    }

    @Test
    @DisplayName("Update server status - SHUTDOWN")
    void testUpdateServerStateShutdown() throws InterruptedException {
        window.updateUI(ServerState.SHUTDOWN);
        verifyNotChangedComponents();

        waitForState();
        assertThat(startButton.isEnabled(), Matchers.equalTo(true));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(false));
        assertThat(portInput.isEnabled(), Matchers.equalTo(true));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Update server status - WAITING")
    void testUpdateServerStateWaiting() throws InterruptedException {
        window.updateUI(ServerState.WAITING);
        verifyNotChangedComponents();

        waitForState();
        assertThat(startButton.isEnabled(), Matchers.equalTo(false));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(true));
        assertThat(portInput.isEnabled(), Matchers.equalTo(false));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Update server status - CONNECTED")
    void testUpdateServerStateConnected() throws InterruptedException {
        window.updateUI(ServerState.CONNECTED);
        verifyNotChangedComponents();

        waitForState();
        assertThat(startButton.isEnabled(), Matchers.equalTo(false));
        assertThat(stopButton.isEnabled(), Matchers.equalTo(true));
        assertThat(portInput.isEnabled(), Matchers.equalTo(false));
        assertThat(passwordInput.isEnabled(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("UI configuration")
    void testUIConfig() {
        window.startUI();

        verify(appender).setEnableOutput(true);
        verify(window).setTitle("Controller server");
        verify(window).setSize(400, 600);
        verify(window).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        verify(window).setVisible(true);
        verify(window).setLayout(any(BorderLayout.class));
        verify(window).add(settingsPanel, BorderLayout.NORTH);
        verify(window).add(logPanel);
        verify(window).setLocationRelativeTo(null);
        verify(window).addWindowListener(any(ServerWindowAdapter.class));

        verify(window).revalidate();
        verify(appender).flush();
    }

    /**
     * Verifies components not affected by server state.
     */
    private void verifyNotChangedComponents() {
        assertThat(autostart.isEnabled(), Matchers.equalTo(true));
        assertThat(ipCombobox.isEnabled(), Matchers.equalTo(true));
        assertThat(logLevelsCombobox.isEnabled(), Matchers.equalTo(true));
    }

    /**
     * Waits a while for AWT.
     *
     * @throws InterruptedException
     *             the InterruptedException
     */
    private void waitForAwt() throws InterruptedException {
        Thread.sleep(50);
    }

    /**
     * Waits for server to change state.
     *
     * @throws InterruptedException
     *             InterruptedException
     */
    private void waitForState() throws InterruptedException {
        Thread.sleep(500);
    }

}
