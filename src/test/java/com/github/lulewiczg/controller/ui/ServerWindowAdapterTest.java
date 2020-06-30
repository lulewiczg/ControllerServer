package com.github.lulewiczg.controller.ui;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import javax.swing.*;

import static org.mockito.Mockito.*;

/**
 * Tests ServerWindowAdapter.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { AWTTestConfiguration.class, MockPropertiesConfiguration.class, MockRequiredUIConfiguration.class,
        SettingsComponent.class, ExceptionLoggingService.class, ServerWindowAdapter.class, })
@EnableAutoConfiguration
class ServerWindowAdapterTest {

    @SpyBean
    private ServerWindowAdapter adapter;

    @MockBean
    private ControllerServerManager manager;

    @MockBean
    private SettingsComponent settings;

    @Autowired
    private SwingPopup popup;

    @BeforeEach
    public void before() {
        doNothing().when(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is not running")
    void testExitServerStopped() {
        when(manager.isRunning()).thenReturn(false);

        adapter.windowClosing(null);

        verify(settings).saveSettings();
        verify(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is running")
    void testExitServerRunning() {
        when(manager.isRunning()).thenReturn(true);
        when(popup.showExitConfirm()).thenReturn(JOptionPane.YES_OPTION);

        adapter.windowClosing(null);

        verify(settings).saveSettings();
        verify(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is running and cancel")
    void testExitServerRunningCancel() {
        when(manager.isRunning()).thenReturn(true);
        when(popup.showExitConfirm()).thenReturn(JOptionPane.NO_OPTION);

        adapter.windowClosing(null);

        verify(settings, never()).saveSettings();
        verify(adapter, never()).exit();
    }

}
