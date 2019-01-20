package com.github.lulewiczg.controller.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Component for displaying logs.
 *
 * @author Grzegurz
 */
@Plugin(name = "JTextAreaAppender", category = "Core", elementType = "appender", printObject = true)
public class JTextAreaAppender extends AbstractAppender {

    private static volatile List<JTextArea> jTextAreaList = new ArrayList<>();

    private int maxLines = 0;

    protected JTextAreaAppender(String name, Layout<?> layout, Filter filter, int maxLines, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.maxLines = maxLines;
    }

    @PluginFactory
    public static JTextAreaAppender createAppender(@PluginAttribute("name") String name,
            @PluginAttribute("maxLines") int maxLines, @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("Layout") Layout<?> layout, @PluginElement("Filters") Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for JTextAreaAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new JTextAreaAppender(name, layout, filter, maxLines, ignoreExceptions);
    }

    /**
     * Adds text area to push logs.
     *
     * @param textArea
     *            text area
     */
    public static void addTextArea(JTextArea textArea) {
        jTextAreaList.add(textArea);
    }

    /*
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    @Override
    public void append(LogEvent event) {
        String message = new String(this.getLayout().toByteArray(event));
        SwingUtilities.invokeLater(() -> {
            for (JTextArea ta : jTextAreaList) {
                try {
                    if (ta != null) {
                        if (ta.getText().length() == 0) {
                            ta.setText(message);
                        } else {
                            ta.append("\n" + message);
                            if (maxLines > 0 & ta.getLineCount() > maxLines + 1) {
                                int endIdx = ta.getDocument().getText(0, ta.getDocument().getLength()).indexOf("\n", 0);
                                ta.getDocument().remove(0, endIdx + 1);
                            }
                        }
                        String content = ta.getText();
                        ta.setText(content.substring(0, content.length() - 1));
                    }
                } catch (BadLocationException e) {
                    System.out.println("Unable to append log to text area: " + e.getMessage());
                }
            }
        });
    }
}