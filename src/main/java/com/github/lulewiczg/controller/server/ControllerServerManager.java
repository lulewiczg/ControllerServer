package com.github.lulewiczg.controller.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.exception.ServerAlreadyStoppedException;

/**
 * Manager for ControllerServer. Manages stopping and restarting server.
 *
 * @author Grzegurz
 */
@Lazy
@Service
@DependsOn({ "JTextAreaAppender" })
public class ControllerServerManager {

    private static final Logger log = LogManager.getLogger(ControllerServerManager.class);
    private ExecutorService stateRunner = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService restarterRunner = Executors.newScheduledThreadPool(1);
    private Future<?> runningThread;

    @Autowired
    private ControllerServer server;

    @PostConstruct
    private void setupRunner() {
        restarterRunner.scheduleWithFixedDelay(() -> {
            if (server.getInternalState() == InternalServerState.DOWN && runningThread != null && runningThread.isDone()) {
                log.info("Restarting server...");
                start();
            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts server.
     *
     * @return thread
     */
    public Future<?> start() {
        if (server.getInternalState() == InternalServerState.UP) {
            throw new ServerAlreadyRunningException();
        }
        runningThread = stateRunner.submit(() -> server.start());
        return runningThread;
    }

    /**
     * Stops server.
     */
    public void stop() {
        if (server.getInternalState() != InternalServerState.UP) {
            throw new ServerAlreadyStoppedException();
        }
        stateRunner.submit(() -> server.stop());
    }

    /**
     * Checks if server is running;
     *
     * @return true if running
     */
    public boolean isRunning() {
        return server.getInternalState() == InternalServerState.UP;
    }

    public ServerState getStatus() {
        return server.getStatus();
    }

}
