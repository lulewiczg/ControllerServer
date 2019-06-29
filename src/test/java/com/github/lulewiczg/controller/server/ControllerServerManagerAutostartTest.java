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

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.MockUtilConfiguration;

/**
 * Tests ControllerServerManager.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testAutostart")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { MockPropertiesConfiguration.class, MockRequiredUIConfiguration.class, MockUtilConfiguration.class,
        ControllerServer.class, ControllerServerManager.class })
@EnableAutoConfiguration
public class ControllerServerManagerAutostartTest {

    @MockBean
    private ControllerServer server;

    @Autowired
    protected ControllerServerManager manager;

    @BeforeEach
    public void before() {
        Mockito.when(server.getStatus()).thenReturn(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server autostart")
    public void testStart() throws Exception {
        Thread.sleep(500);
        Mockito.verify(server).start();
    }

}
