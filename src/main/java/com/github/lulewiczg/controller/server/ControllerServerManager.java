package com.github.lulewiczg.controller.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Manager for ControllerServer. Manages stopping and restarting server.
 *
 * @author Grzegurz
 */
@Lazy
@Service
@DependsOn({ "jTextAreaAppender" })
public class ControllerServerManager {

    private static final Logger log = LogManager.getLogger(ControllerServerManager.class);
    private ExecutorService stateRunner = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService restarterRunner = Executors.newScheduledThreadPool(1);
    private Future<?> runningThread;

    @Autowired
    private ControllerServer server;

    public ControllerServerManager() {
        restarterRunner.scheduleWithFixedDelay(() -> {
            if (server.getInternalState() == InternalServerState.DOWN && runningThread != null && runningThread.isDone()) {
                log.info("Restarting server...");
                start();
            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts server.
     */
    public void start() {
        runningThread = stateRunner.submit(() -> server.start());
    }

    /**
     * Stops server.
     */
    public void stop() {
        stateRunner.submit(() -> server.stop());
    }

    public ServerState getStatus() {
        return server.getStatus();
    }

}
