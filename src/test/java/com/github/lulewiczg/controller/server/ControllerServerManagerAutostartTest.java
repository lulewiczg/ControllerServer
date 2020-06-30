package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.MockUtilConfiguration;
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

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests ControllerServerManager.
 *
 * @author Grzegurz
 */
@ActiveProfiles("testAutostart")
@SpringBootTest(classes = { MockPropertiesConfiguration.class, MockRequiredUIConfiguration.class, MockUtilConfiguration.class,
        ControllerServer.class, ControllerServerManager.class })
@EnableAutoConfiguration
class ControllerServerManagerAutostartTest {

    @MockBean
    private ControllerServer server;

    @Autowired
    protected ControllerServerManager manager;

    @BeforeEach
    public void before() {
        when(server.getStatus()).thenReturn(ServerState.SHUTDOWN);
    }

    @Test
    @DisplayName("Server autostart")
    void testStart() {
        mockServerStart();
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> server.getStatus().isRunning());

        verify(server).start();
    }

    /**
     * Mocks server to change state to UP when started.
     */
    private void mockServerStart() {
        when(server.getStatus()).thenAnswer(i -> {
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
