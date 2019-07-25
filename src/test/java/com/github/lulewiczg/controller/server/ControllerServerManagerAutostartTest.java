package com.github.lulewiczg.controller.server;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.MockUtilConfiguration;

/**
 * Tests ControllerServerManager.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testAutostart")
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
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> server.getStatus().isRunning());

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
