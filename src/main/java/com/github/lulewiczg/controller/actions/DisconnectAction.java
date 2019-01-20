package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.exception.DisconnectException;

public class DisconnectAction extends Action {

    private static final long serialVersionUID = 1L;

    @Override
    public void doAction() {
        throw new DisconnectException();
    }

}
