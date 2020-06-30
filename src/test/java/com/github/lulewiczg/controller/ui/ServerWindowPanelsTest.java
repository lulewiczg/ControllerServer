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
    @DisplayName("Settings panel is created properly")
    void testSettingsPanel() {
        assertThat(settingsPanel.getComponentCount(), Matchers.equalTo(14));

        JLabel label = (JLabel) settingsPanel.getComponent(0);
        assertThat(label.getText(), Matchers.equalTo("IP"));
        assertThat(settingsPanel.getComponent(1), Matchers.equalTo(ipCombobox));

        JLabel label2 = (JLabel) settingsPanel.getComponent(2);
        assertThat(label2.getText(), Matchers.equalTo("Port"));
        assertThat(settingsPanel.getComponent(3), Matchers.equalTo(portInput));

        JLabel label3 = (JLabel) settingsPanel.getComponent(4);
        assertThat(label3.getText(), Matchers.equalTo("Password"));
        assertThat(settingsPanel.getComponent(5), Matchers.equalTo(passwordInput));

        JLabel label4 = (JLabel) settingsPanel.getComponent(6);
        assertThat(label4.getText(), Matchers.equalTo("Timeout"));
        assertThat(settingsPanel.getComponent(7), Matchers.equalTo(timeoutInput));

        assertThat(settingsPanel.getComponent(8), Matchers.equalTo(autostart));

        JLabel label5 = (JLabel) settingsPanel.getComponent(10);
        assertThat(label5.getText(), Matchers.equalTo("Server state"));
        assertThat(settingsPanel.getComponent(11), Matchers.equalTo(stateIndicator));

        assertThat(settingsPanel.getComponent(12), Matchers.equalTo(stopButton));
        assertThat(settingsPanel.getComponent(13), Matchers.equalTo(startButton));
    }

}
