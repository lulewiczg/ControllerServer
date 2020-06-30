package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.exception.ActionException;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Abstract action that requires login.
 *
 * @author Grzegurz
 */

public abstract class LoginRequiredAction extends Action {
    private static final long serialVersionUID = 1L;

    /**
     * Throws exception when action should not be executed.
     *
     * @throws ActionException
     *             the ActionException
     */
    @Override
    protected void doThrowException() throws ActionException {
        throw new AuthorizationException(String.format("Action %s requires login", getClass().getSimpleName()));
    }
}
