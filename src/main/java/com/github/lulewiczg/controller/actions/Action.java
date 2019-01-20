package com.github.lulewiczg.controller.actions;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.Serializable;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.common.FakeRobot;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;

public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final Logger log = LogManager.getLogger();

    protected static Robot robot;
    protected EnumSet<ServerState> states;// TODO

    protected abstract Response doAction(ControllerServer server) throws ActionException;

    public Action() {
        this.states = EnumSet.of(ServerState.CONNECTED);
    }

    /**
     * Checks server state and runs action
     *
     * @param server
     *            server
     * @return action response
     * @throws ActionException
     *             the ActionException
     */
    public Response run(ControllerServer server) throws ActionException {
        if (states.contains(server.getStatus())) {
            return doAction(server);
        } else {
            throw new ActionException("Invalid state for action");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Action: ").append(this.getClass().getSimpleName());
        return str.toString();
    }

    /**
     * Sets actions to test mode.
     */
    public static void setTestMode() {
        try {
            robot = new FakeRobot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets actions to normal mode.
     */
    public static void setNormalMode() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
}
