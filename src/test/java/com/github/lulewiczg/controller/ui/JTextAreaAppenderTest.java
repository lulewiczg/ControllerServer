package com.github.lulewiczg.controller.ui;

import static org.junit.Assert.assertThat;

import javax.swing.JTextArea;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Tests JTextAreaAppender class
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { AWTTestConfiguration.class, SettingsComponent.class, MockRequiredUIConfiguration.class })
@EnableAutoConfiguration
public class JTextAreaAppenderTest {

    private static final String TEST_LOG = "Test Log";

    @SpyBean
    private JTextAreaAppender appender;

    @Autowired
    private JTextArea textArea;

    @MockBean
    private SettingsComponent settings;

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

    @Test
    @DisplayName("Filter is updated to proper level")
    public void testFilterUpdate() throws Exception {
        Level level = Level.WARN;
        Filter filter = appender.getFilter();

        appender.updateFilter(level);

        Mockito.verify(appender).removeFilter(filter);
        ArgumentCaptor<ThresholdFilter> argument = ArgumentCaptor.forClass(ThresholdFilter.class);
        Mockito.verify(appender).addFilter(argument.capture());
        assertThat(argument.getValue().getLevel(), Matchers.equalTo(level));
        assertThat(argument.getValue().getOnMatch(), Matchers.equalTo(Result.ACCEPT));
        assertThat(argument.getValue().getOnMismatch(), Matchers.equalTo(Result.DENY));
    }

}
