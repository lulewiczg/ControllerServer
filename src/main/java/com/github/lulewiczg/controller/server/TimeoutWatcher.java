package com.github.lulewiczg.controller.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Class for timeout checking.
 *
 * @author Grzegorz
 */
@Log4j2
@Component
public class TimeoutWatcher {

    @Value("${com.github.lulewiczg.setting.serverTimeout:120000}")
    private long timeout;

    public void watch(ControllerServer server) {
        // System.out.println(server.getStatus());
        if (server.getStatus() == ServerState.CONNECTED && server.getLastAcionTime() != 0
                && System.currentTimeMillis() - server.getLastAcionTime() > timeout) {
            log.info("Client connection timeout");
            server.logout();
        }
    }
}
