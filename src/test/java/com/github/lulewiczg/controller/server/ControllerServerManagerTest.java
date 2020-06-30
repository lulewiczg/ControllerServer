package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockRequiredUIConfiguration;
import com.github.lulewiczg.controller.MockServerConfiguration;
import com.github.lulewiczg.controller.TestUtilConfiguration;
import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.exception.ServerAlreadyStoppedException;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests controller ControllerServerManager.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockServerConfiguration.class, MockRequiredUIConfiguration.class, MockPropertiesConfiguration.class,
        TestUtilConfiguration.class, ControllerServerManager.class, ObjectOutputStream.class })
@EnableAutoConfiguration
class ControllerServerManagerTest {

    @Autowired
    private ControllerServerManager serverRunner;

    @MockBean
    private ControllerServer server;

    @BeforeEach
    public void before() {
        when(server.getStatus()).thenReturn(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server start")
    void testStart() {
        mockServerStart();

        serverRunner.start();

        waitForServer();
        verify(server).start();
    }

    @Test
    @DisplayName("Server start when already started")
    void testStartTwice() {
        when(server.getStatus()).thenReturn(ServerState.WAITING);

        assertThrows(ServerAlreadyRunningException.class, () -> serverRunner.start());

        verify(server, never()).start();
    }

    @Test
    @DisplayName("Server stop")
    void testStop() {
        when(server.getStatus()).thenReturn(ServerState.WAITING);

        serverRunner.stop();

        waitForServer();
        verify(server).stop();
    }

    @Test
    @DisplayName("Server stop when stopped")
    void testStopWhenStopped() {
        assertThrows(ServerAlreadyStoppedException.class, () -> serverRunner.stop());

        verify(server, never()).stop();
    }

    @Test
    @DisplayName("Server stop when down")
    void testStopWhenDown() {
        mockServerStart();
        assertThrows(ServerAlreadyStoppedException.class, () -> serverRunner.stop());

        verify(server, never()).stop();
    }

    @MethodSource
    @DisplayName("Server running check")
    @ParameterizedTest(name = "''{0}'' state is running: ''{1}''")
    void testActionHandledException(ServerState state, boolean running) {
        when(server.getStatus()).thenReturn(state);

        assertThat(serverRunner.isRunning(), Matchers.equalTo(running));
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

    /**
     * Creates test parameters.
     *
     * @return parameters
     */
    private static Stream<Arguments> testActionHandledException() {
        return Stream.of(Arguments.of(ServerState.WAITING, true), Arguments.of(ServerState.CONNECTED, true),
                Arguments.of(ServerState.SHUTDOWN, false), Arguments.of(ServerState.FORCED_SHUTDOWN, false));
    }

    /**
     * Waits for server to change state.
     *
     */
    private void waitForServer() {
        Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() -> server.getStatus().isRunning());
    }
}
