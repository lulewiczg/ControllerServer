package com.github.lulewiczg.controller.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;
import com.github.lulewiczg.controller.UIConfiguration;

/**
 * Tests controller ControllerServerManager.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { UIConfiguration.class, TestConfiguration.class })
@EnableAutoConfiguration
public class ControllerServerManagerAutostartTest {

    @MockBean
    private ControllerServer server;

    @Autowired
    private SettingsComponent settings;

    @BeforeEach
    public void before() {
        Mockito.when(settings.isAutostart()).thenReturn(true);
    }

    @Test
    @DisplayName("Server autostart")
    public void testStart() throws Exception {
        Thread.sleep(200);
        Mockito.verify(server).start();
    }

}
