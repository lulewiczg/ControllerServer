package com.github.lulewiczg.controller.ui;

import java.nio.charset.Charset;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Component for displaying logs.
 *
 * @author Grzegurz
 */
@Component
@Lazy
@Plugin(name = "JTextAreaAppender", category = "Core", elementType = "appender", printObject = true)
public class JTextAreaAppender extends AbstractAppender {

    private JTextArea textArea;

    protected JTextAreaAppender(JTextArea textArea,
            @Value("${com.github.lulewiczg.logging.pattern}") String logPattern) {
        super("SWING_APPENDER", null,
                PatternLayout.newBuilder().withPattern(logPattern).withCharset(Charset.defaultCharset()).build(), false,
                null);
        this.textArea = textArea;
    }

    /*
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    @Override
    public void append(LogEvent event) {
        if (!textArea.isDisplayable()) {
            return;
        }
        String message = new String(this.getLayout().toByteArray(event));
        SwingUtilities.invokeLater(() -> textArea.append(message));
    }
}