package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.AWTTestConfiguration;
import com.github.lulewiczg.controller.EagerConfiguration;
import com.github.lulewiczg.controller.MainConfiguration;
import com.github.lulewiczg.controller.TestUtilConfiguration;
import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.connection.JSONClientConnection;
import com.github.lulewiczg.controller.actions.processor.mouse.JNAMouseMovingService;
import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.client.JsonClient;
import com.github.lulewiczg.controller.ui.JTextAreaAppender;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests controller server.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
@SpringBootTest(classes = {AWTTestConfiguration.class, EagerConfiguration.class, MainConfiguration.class,
        ControllerServerManager.class, TestUtilConfiguration.class, JNAMouseMovingService.class, JTextAreaAppender.class,
        ControllerServer.class, JSONClientConnection.class, ActionProcessor.class, TimeoutWatcher.class, JsonClient.class})
class ControllerServerIntegJSONTest extends ControllerServerIntegTest {

    @Override
    protected Client getClient(int port) {
        return context.getBean(JsonClient.class, port);
    }
}
