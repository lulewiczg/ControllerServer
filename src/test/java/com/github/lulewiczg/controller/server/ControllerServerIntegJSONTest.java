package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.client.JsonClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests controller server with JSON implementation..
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
@ActiveProfiles("testIntegJson")
class ControllerServerIntegJSONTest extends ControllerServerIntegTest {

    @Override
    protected Client getClient(int port) {
        return new JsonClient(port);
    }
}
