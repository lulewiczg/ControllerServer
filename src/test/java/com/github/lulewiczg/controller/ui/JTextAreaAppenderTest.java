package com.github.lulewiczg.controller.ui;

import javax.swing.JTextArea;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;

/**
 * Tests JTextAreaAppender class
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AWTTestConfiguration.class, MockRequiredUIConfiguration.class })
@EnableAutoConfiguration
public class JTextAreaAppenderTest {

    private static final String TEST_LOG = "Test Log";

    @SpyBean
    private JTextAreaAppender appender;

    @Autowired
    private JTextArea textArea;

    private Log4jLogEvent event = new Log4jLogEvent("test logger", null, this.getClass().getSimpleName(), Level.INFO,
            new SimpleMessage(TEST_LOG), null, null);

    @Test
    @DisplayName("Appender is not appending logs when texteara is not rendered")
    public void testAppendTextAreaNotVisible() throws Exception {
        Mockito.when(textArea.isDisplayable()).thenReturn(false);

        appender.append(event);
        appender.append(event);

        Mockito.verify(appender, Mockito.never()).flush();
    }

    @Test
    @DisplayName("Appender is not appending when disabled")
    public void testAppendOutputDisabled() throws Exception {
        Mockito.when(textArea.isDisplayable()).thenReturn(true);

        appender.append(event);
        appender.append(event);

        Mockito.verify(appender, Mockito.never()).flush();
    }

    @Test
    @DisplayName("Appender is appending logs to textarea")
    public void textAreaAppend() throws Exception {
        Mockito.when(textArea.isDisplayable()).thenReturn(true);
        Mockito.when(appender.isEnableOutput()).thenReturn(true);
        appender.start();

        appender.append(event);
        appender.append(event);

        Mockito.verify(appender, Mockito.times(2)).flush();
    }

}
