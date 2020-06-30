package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.exception.ServerAlreadyRunningException;
import com.github.lulewiczg.controller.exception.ServerAlreadyStoppedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * Manager for ControllerServer. Manages stopping and restarting server.
 *
 * @author Grzegurz
 */
@Lazy
@Service
@DependsOn({"JTextAreaAppender"})
public class ControllerServerManager {

    private static final Logger log = LogManager.getLogger(ControllerServerManager.class);
    private final ExecutorService serverRunner = Executors.newSingleThreadExecutor();
    private final ExecutorService jobRunner = Executors.newCachedThreadPool();
    private final ScheduledExecutorService restartedRunner = Executors.newScheduledThreadPool(1);
    private Future<?> runningThread;

    @Autowired
    private ControllerServer server;

    @Autowired
    private SettingsComponent settings;

    @PostConstruct
    private void setupRunner() {
        restartedRunner.scheduleWithFixedDelay(() -> {
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
        if (settings.isAutostart() && runningThread == null) {
            return true;
        }
        return runningThread != null && runningThread.isDone() && server.getStatus() == ServerState.SHUTDOWN;
    }

    /**
     * Starts server.
     */
    public void start() {
        if (server.getStatus().isRunning()) {
            throw new ServerAlreadyRunningException();
        }
        runningThread = serverRunner.submit(new ControllerServerThread(server::start));
    }

    /**
     * Stops server.
     */
    public void stop() {
        if (!server.getStatus().isRunning()) {
            throw new ServerAlreadyStoppedException();
        }
        jobRunner.submit(() -> server.stop());
    }

    /**
     * Checks if server is running;
     *
     * @return true if running
     */
    public boolean isRunning() {
        return server.getStatus().isRunning();
    }

    public ServerState getStatus() {
        return server.getStatus();
    }

    /**
     * Class for handling server thread, handling non-interruptible socket.
     *
     * @author Grzegorz
     */
    class ControllerServerThread extends Thread {

        private final Runnable lambda;

        private boolean sockedInterrupted;

        ControllerServerThread(Runnable lambda) {
            this.lambda = lambda;
        }

        /**
         * Runs lambda expression.
         */
        @Override
        public void run() {
            this.lambda.run();
        }

        /**
         * Fixes Socket blocking read, which ignores interruptions.
         */
        @Override
        public void interrupt() {
            super.interrupt();
            if (!sockedInterrupted) {
                server.closeServer();
                sockedInterrupted = true;
            }
        }
    }
}
