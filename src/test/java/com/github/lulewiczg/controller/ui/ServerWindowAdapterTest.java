package com.github.lulewiczg.controller.ui;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.server.ControllerServerManager;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.SettingsComponent;

/**
 * Tests ServerWindowAdapter.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AWTTestConfiguration.class, MockPropertiesConfiguration.class, MockRequiredUIConfiguration.class,
        SettingsComponent.class, ExceptionLoggingService.class, ServerWindowAdapter.class, })
@EnableAutoConfiguration
public class ServerWindowAdapterTest {

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
        Mockito.doNothing().when(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is not running")
    public void testExitServerStopped() throws InterruptedException {
        Mockito.when(manager.isRunning()).thenReturn(false);

        adapter.windowClosing(null);

        Mockito.verify(settings).saveSettings();
        Mockito.verify(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is running")
    public void testExitServerRunning() throws InterruptedException {
        Mockito.when(manager.isRunning()).thenReturn(true);
        Mockito.when(popup.showExitConfirm()).thenReturn(JOptionPane.YES_OPTION);

        adapter.windowClosing(null);

        Mockito.verify(settings).saveSettings();
        Mockito.verify(adapter).exit();
    }

    @Test
    @DisplayName("App exit when server is running and cancel")
    public void testExitServerRunningCancel() throws InterruptedException {
        Mockito.when(manager.isRunning()).thenReturn(true);
        Mockito.when(popup.showExitConfirm()).thenReturn(JOptionPane.NO_OPTION);

        adapter.windowClosing(null);

        Mockito.verify(settings, Mockito.never()).saveSettings();
        Mockito.verify(adapter, Mockito.never()).exit();
    }

}
