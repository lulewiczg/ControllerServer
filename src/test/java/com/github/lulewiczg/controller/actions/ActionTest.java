package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.MockServerConfiguration;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;
import com.github.lulewiczg.controller.server.ServerState;
import com.github.lulewiczg.controller.server.SettingsComponent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.EnumSet;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests ActionTest.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockPropertiesConfiguration.class, MockServerConfiguration.class, ControllingService.class,
        SettingsComponent.class, ExceptionLoggingService.class, })
@EnableAutoConfiguration
class ActionTest {

    @Spy
    private Action action;

    @Autowired
    private ControllerServer server;

    @Autowired
    private ControllingService controllingService;

    @Test
    @DisplayName("Action run in proper state")
    void testRunInProperState() throws Exception {
        when(action.getProperStates()).thenReturn(EnumSet.of(ServerState.WAITING));
        when(server.getStatus()).thenReturn(ServerState.WAITING);

        action.run(server, controllingService);

        verify(action).doAction(controllingService);
    }

    @Test
    @DisplayName("Action run in inproper state")
    void testRunInImproperState() throws Exception {
        when(action.getProperStates()).thenReturn(EnumSet.of(ServerState.CONNECTED, ServerState.SHUTDOWN));
        when(server.getStatus()).thenReturn(ServerState.WAITING);

        action.run(server, controllingService);

        verify(action).doThrowException();
    }

    @Test
    @DisplayName("Action requires CONNECTED state by default")
    void testProperStates() {
        EnumSet<ServerState> properStates = action.getProperStates();

        assertThat(properStates, Matchers.hasSize(1));
        assertThat(properStates, Matchers.contains(ServerState.CONNECTED));
    }
}
