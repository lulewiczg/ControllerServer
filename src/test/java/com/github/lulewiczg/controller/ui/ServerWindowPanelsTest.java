package com.github.lulewiczg.controller.ui;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import javax.swing.*;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Grzegorz
 *
 *         Tests panels config.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { AWTTestConfiguration.class, EagerConfiguration.class, UIConfiguration.class, ServerWindow.class,
        ServerWindowAdapter.class })
@EnableAutoConfiguration
class ServerWindowPanelsTest {

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
    private JComboBox<UIConfiguration.ComboboxEntry> connectionTypeCombobox;

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
    private JLabel stateIndicator;

    @Autowired
    private JButton startButton;

    @Autowired
    private JButton stopButton;

    @Test
    @DisplayName("Logs panel is created properly")
    void testLogsPanel() throws Exception {
        assertThat(logPanel.getComponentCount(), equalTo(2));

        JPanel panel = (JPanel) logPanel.getComponent(0);
        assertThat(panel.getComponentCount(), equalTo(2));
        assertThat(panel.getComponent(0), equalTo(clearLogsButton));
        assertThat(panel.getComponent(1), equalTo(logLevelsCombobox));

        JScrollPane panel2 = (JScrollPane) logPanel.getComponent(1);
        assertThat(panel2.getComponentCount(), Matchers.greaterThanOrEqualTo(1));
        JViewport view = (JViewport) panel2.getComponent(0);
        assertThat(view.getComponentCount(), equalTo(1));
        assertThat(view.getComponent(0), equalTo(textArea));
    }

    @Test
    @DisplayName("Settings panel is created properly")
    void testSettingsPanel() {
        assertThat(settingsPanel.getComponentCount(), equalTo(16));

        JLabel label = (JLabel) settingsPanel.getComponent(0);
        assertThat(label.getText(), equalTo("IP"));
        assertThat(settingsPanel.getComponent(1), equalTo(ipCombobox));

        JLabel label2 = (JLabel) settingsPanel.getComponent(2);
        assertThat(label2.getText(), equalTo("Port"));
        assertThat(settingsPanel.getComponent(3), equalTo(portInput));

        JLabel label3 = (JLabel) settingsPanel.getComponent(4);
        assertThat(label3.getText(), equalTo("Password"));
        assertThat(settingsPanel.getComponent(5), equalTo(passwordInput));

        JLabel label4 = (JLabel) settingsPanel.getComponent(6);
        assertThat(label4.getText(), equalTo("Timeout"));
        assertThat(settingsPanel.getComponent(7), equalTo(timeoutInput));

        JLabel label5 = (JLabel) settingsPanel.getComponent(8);
        assertThat(label5.getText(), equalTo("Connection Type"));
        assertThat(settingsPanel.getComponent(9), equalTo(connectionTypeCombobox));

        assertThat(settingsPanel.getComponent(10), equalTo(autostart));

        JLabel label6 = (JLabel) settingsPanel.getComponent(12);
        assertThat(label6.getText(), equalTo("Server state"));
        assertThat(settingsPanel.getComponent(13), equalTo(stateIndicator));

        assertThat(settingsPanel.getComponent(14), equalTo(stopButton));
        assertThat(settingsPanel.getComponent(15), equalTo(startButton));
    }

}
