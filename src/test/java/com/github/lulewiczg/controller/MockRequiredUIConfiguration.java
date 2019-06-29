package com.github.lulewiczg.controller;

import javax.swing.JTextArea;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;

import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import com.github.lulewiczg.controller.ui.SwingPopup;

/**
 * Configuration for mocking required UI components.
 *
 * @author Grzegurz
 */
@Configuration
public class MockRequiredUIConfiguration {
    @MockBean(name = "JTextAreaAppender")
    private JTextAreaAppender appender;

    @MockBean
    private SwingPopup popup;

    @SpyBean
    private JTextArea textArea;

}
