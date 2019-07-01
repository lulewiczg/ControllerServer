package com.github.lulewiczg.controller.ui;

import java.nio.charset.Charset;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Component for displaying logs.
 *
 * @author Grzegurz
 */
@Component
@Plugin(name = "JTextAreaAppender", category = "Core", elementType = "appender", printObject = true)
public class JTextAreaAppender extends AbstractAppender {

    private JTextArea textArea;

    private StringBuffer buffer = new StringBuffer();

    private boolean enableOutput;

    @Autowired
    public JTextAreaAppender(JTextArea textArea, SettingsComponent settings,
            @Value("${com.github.lulewiczg.logging.pattern}") String logPattern) {
        super("SWING_APPENDER", null,
                PatternLayout.newBuilder().withPattern(logPattern).withCharset(Charset.defaultCharset()).build(), false, null);
        this.textArea = textArea;
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("com.github.lulewiczg.controller");
        loggerConfig.addAppender(this, null, null);
        updateFilter(settings.getLogLevel());
        config.addAppender(this);
        start();
        ctx.updateLoggers(config);
    }

    public void updateFilter(Level level) {
        removeFilter(getFilter());
        addFilter(ThresholdFilter.createFilter(level, Result.ACCEPT, Result.DENY));
    }

    /*
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    @Override
    public void append(LogEvent event) {
        String message = new String(this.getLayout().toByteArray(event));
        buffer.append(message);
        if (!isEnableOutput() || !textArea.isDisplayable()) {
            return;
        }
        flush();
    }

    /**
     * Flushes collected logs to component.
     */
    public void flush() {
        String logMsg = buffer.toString();
        buffer.delete(0, buffer.length());
        SwingUtilities.invokeLater(() -> textArea.append(logMsg));
    }

    public void setEnableOutput(boolean enableOutput) {
        this.enableOutput = enableOutput;
    }

    public boolean isEnableOutput() {
        return enableOutput;
    }

}