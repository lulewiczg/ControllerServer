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

/**
 * Abstract action to execute on server.
 *
 * @author Grzegurz
 */
public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;
    protected static transient final Logger log = LogManager.getLogger();
    protected static transient Robot robot;
    protected EnumSet<ServerState> states;// TODO

    public Action() {
        this.states = EnumSet.of(ServerState.CONNECTED);
    }

    /**
     * Executes action.
     *
     * @param server
     *            server
     * @return action result
     * @throws ActionException
     *             the ActionException
     */
    protected abstract Response doAction(ControllerServer server) throws ActionException;

    /**
     * Throws exception when action can not be run.
     *
     * @throws ActionException
     *             the ActionException
     */
    protected abstract void doThrowException() throws ActionException;

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
            doThrowException();
            return null;
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
