package com.github.lulewiczg.controller.actions;

/**
 * Abstract action for mouse button event.
 *
 * @author Grzegurz
 */
public abstract class MouseButtonAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;
    protected int key;

    protected MouseButtonAction() {
        super();
    }

    public MouseButtonAction(int key) {
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
