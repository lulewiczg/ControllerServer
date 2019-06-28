package com.github.lulewiczg.controller.server;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;
import com.github.lulewiczg.controller.actions.processor.connection.ObjectStreamClientConnection;
import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.exception.ServerAlreadyStoppedException;

/**
 * Tests controller ControllerServerManager.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { TestConfiguration.class, ObjectStreamClientConnection.class })
@EnableAutoConfiguration
public class ControllerServerManagerTest {

    @Autowired
    private ControllerServerManager serverRunner;

    @MockBean
    private ControllerServer server;

    @BeforeEach
    public void before() {
        Mockito.when(server.getStatus()).thenReturn(ServerState.FORCED_SHUTDOWN);
    }

    @Test
    @DisplayName("Server start")
    public void testStart() throws Exception {
        mockServerStart();

        serverRunner.start();

        Thread.sleep(100);
        Mockito.verify(server).start();
    }

    @Test
    @DisplayName("Server start after stop")
    public void testStartAfterStop() throws Exception {
        serverRunner.start();

        Thread.sleep(100);
        Mockito.verify(server).start();
    }

    @Test
    @DisplayName("Server start when already started")
    public void testStartTwice() throws Exception {
        Mockito.when(server.getStatus()).thenReturn(ServerState.WAITING);

        assertThrows(ServerAlreadyRunningException.class, () -> serverRunner.start());

        Mockito.verify(server, Mockito.never()).start();
    }

    @Test
    @DisplayName("Server stop")
    public void testStop() throws Exception {
        Mockito.when(server.getStatus()).thenReturn(ServerState.WAITING);

        serverRunner.stop();

        Thread.sleep(100);
        Mockito.verify(server).stop();
    }

    @Test
    @DisplayName("Server stop when stopped")
    public void testStopWhenStopped() throws Exception {
        assertThrows(ServerAlreadyStoppedException.class, () -> serverRunner.stop());

        Mockito.verify(server, Mockito.never()).stop();
    }

    @Test
    @DisplayName("Server stop when down")
    public void testStopWhenDown() throws Exception {
        mockServerStart();
        assertThrows(ServerAlreadyStoppedException.class, () -> serverRunner.stop());

        Mockito.verify(server, Mockito.never()).stop();
    }

    @MethodSource
    @DisplayName("Server running check")
    @ParameterizedTest(name = "''{0}'' state is running: ''{1}''")
    public void testActionHandledException(ServerState state, boolean running) throws Exception {
        Mockito.when(server.getStatus()).thenReturn(state);

        assertThat(serverRunner.isRunning(), Matchers.equalTo(running));
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

    /**
     * Creates test parameters.
     *
     * @return parameters
     */
    private static Stream<Arguments> testActionHandledException() {
        return Stream.of(Arguments.of(ServerState.WAITING, true), Arguments.of(ServerState.CONNECTED, true),
                Arguments.of(ServerState.SHUTDOWN, false), Arguments.of(ServerState.FORCED_SHUTDOWN, false));
    }
}
