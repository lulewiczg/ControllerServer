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

    @Autowired
    private SettingsComponent settings;

    @PostConstruct
    private void setupRunner() {
        restarterRunner.scheduleWithFixedDelay(() -> {
            if (shouldStart()) {
                log.info("Starting server...");
                start();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if server should start.
     *
     * @return true if should
     */
    private boolean shouldStart() {
        if (runningThread != null && runningThread.isDone() && server.getInternalState() == InternalServerState.DOWN) {
            return true;
        } else if (runningThread == null && settings.isAutostart()) {
            return true;
        }
        return false;
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
        server.setInternalState(InternalServerState.UNDEFINED);
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
        server.setInternalState(InternalServerState.UNDEFINED);
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
