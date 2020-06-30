package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.client.Client;
import com.github.lulewiczg.controller.client.ObjectStreamClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests controller server with object stream implementation.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
@ActiveProfiles("testInteg")
class ControllerServerIntegObjectStreamTest extends ControllerServerIntegTest {

    @Override
    protected Client getClient(int port) {
        return new ObjectStreamClient(port);
    }
}
