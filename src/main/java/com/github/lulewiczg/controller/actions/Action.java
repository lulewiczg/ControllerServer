package com.github.lulewiczg.controller.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.lulewiczg.controller.actions.impl.*;
import com.github.lulewiczg.controller.actions.processor.ControllingService;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.server.ControllerServer;
import com.github.lulewiczg.controller.server.ServerState;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Abstract action to execute on server.
 *
 * @author Grzegurz
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({ @JsonSubTypes.Type(value = LoginAction.class), @JsonSubTypes.Type(value = LoginRequiredAction.class),
        @JsonSubTypes.Type(value = PingAction.class), @JsonSubTypes.Type(value = DisconnectAction.class),
        @JsonSubTypes.Type(value = KeyAction.class), @JsonSubTypes.Type(value = MouseButtonAction.class),
        @JsonSubTypes.Type(value = MouseButtonPressAction.class), @JsonSubTypes.Type(value = MouseButtonReleaseAction.class),
        @JsonSubTypes.Type(value = MouseMoveAction.class), @JsonSubTypes.Type(value = MouseScrollAction.class),
        @JsonSubTypes.Type(value = ServerStopAction.class), @JsonSubTypes.Type(value = TextAction.class),
        @JsonSubTypes.Type(value = KeyPressAction.class), @JsonSubTypes.Type(value = KeyReleaseAction.class) })
public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * @param controllingService
     *            controlling service
     *
     * @return action result
     * @throws ActionException
     *             the ActionException
     */
    protected abstract Response doAction(ControllingService controllingService) throws ActionException;

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
            return doAction(controllingService);
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
