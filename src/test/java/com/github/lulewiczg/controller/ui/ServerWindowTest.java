package com.github.lulewiczg.controller.ui;

import static org.junit.Assert.assertThat;

import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Tests JTextAreaAppender class
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { EagerConfiguration.class, UIConfiguration.class, ServerWindow.class })
@EnableAutoConfiguration
public class ServerWindowTest {

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

    @Autowired
    private ServerWindow window;

    @Autowired
    private JPanel logPanel;

    @Autowired
    private JButton clearLogsButton;

    @Autowired
    private JComboBox<Level> logLevels;

    @Autowired
    private JTextArea textArea;

    @Test
    @DisplayName("Logs panel is created properly")
    public void testAppendTextAreaNotVisible() throws Exception {
        assertThat(logPanel.getComponentCount(), Matchers.equalTo(2));

        JPanel panel = (JPanel) logPanel.getComponent(0);
        assertThat(panel.getComponentCount(), Matchers.equalTo(2));
        assertThat(panel.getComponent(0), Matchers.equalTo(clearLogsButton));
        assertThat(panel.getComponent(1), Matchers.equalTo(logLevels));

        JScrollPane panel2 = (JScrollPane) logPanel.getComponent(1);
        assertThat(panel2.getComponentCount(), Matchers.greaterThanOrEqualTo(1));
        JViewport view = (JViewport) panel2.getComponent(0);
        assertThat(view.getComponentCount(), Matchers.equalTo(1));
        assertThat(view.getComponent(0), Matchers.equalTo(textArea));
    }
}
