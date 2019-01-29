package com.github.lulewiczg.controller.actions;

/**
 * Abstract action for key event.
 *
 * @author Grzegurz
 */
public abstract class KeyAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;
    protected int key;

    public KeyAction(int key) {
        this.key = key;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", ").append(key);
        return str.toString();
    }

}
