package com.github.lulewiczg.controller.actions;

/**
 * Abstract action for key event.
 *
 * @author Grzegurz
 */
public abstract class KeyAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return super.toString() + ", ";
    }

}
