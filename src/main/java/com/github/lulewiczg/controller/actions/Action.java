package com.github.lulewiczg.controller.actions;

import java.io.Serializable;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.lulewiczg.controller.actions.processor.ControllingService;
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
    protected static transient final Logger log = LogManager.getLogger(Action.class);

    /**
     * Returns proper server states for this action.
     *
     * @return proper states
     */
    protected EnumSet<ServerState> getProperStates() {
        return EnumSet.of(ServerState.CONNECTED);
    }

    /**
     * Executes action.
     *
     * @param server
     *            server
     * @param controllingService
     *            controlling service
     * @return action result
     * @throws ActionException
     *             the ActionException
     */
    protected abstract Response doAction(ControllerServer server, ControllingService controllingService) throws ActionException;

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
     * @param controllingService
     *            controlling service
     * @return action response
     * @throws ActionException
     *             the ActionException
     */
    public Response run(ControllerServer server, ControllingService controllingService) throws ActionException {
        if (getProperStates().contains(server.getStatus())) {
            return doAction(server, controllingService);
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

}
