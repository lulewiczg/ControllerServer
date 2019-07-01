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
        mockServerStart();
        Thread.sleep(500);

        Mockito.verify(server).start();
    }

    /**
     * Mocks server to change state to UP when started.
     */
    private void mockServerStart() {
        Mockito.when(server.getStatus()).thenAnswer(i -> {
            long count = Mockito.mockingDetails(server).getInvocations().stream()
                    .filter(j -> j.getMethod().getName().equals("start")).count();
            if (count != 0) {
                return ServerState.WAITING;
            } else {
                return ServerState.SHUTDOWN;
            }
        });
    }

}
