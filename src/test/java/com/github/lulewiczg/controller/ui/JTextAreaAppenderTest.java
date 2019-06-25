package com.github.lulewiczg.controller.ui;

import javax.swing.JTextArea;

import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;

/**
 * Tests JTextAreaAppender class
 * 
 * @author Grzegurz
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { TestConfiguration.class })
@EnableAutoConfiguration
public class JTextAreaAppenderTest {

    @SpyBean
    private JTextAreaAppender appender;

    @MockBean
    private JTextArea textArea;

    @Mock
    private LogEvent event;

    @Test
    @DisplayName("Appender is not appending logs when texteara is not rendered")
    public void testAppendTextAreaNotVisible() throws Exception {
        Mockito.when(textArea.isDisplayable()).thenReturn(false);

        appender.append(event);

        Mockito.verify(textArea, Mockito.never()).setText(Mockito.anyString());
    }
}
