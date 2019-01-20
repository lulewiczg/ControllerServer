package com.github.lulewiczg.controller.actions;

public abstract class MouseButtonAction extends Action {

    private static final long serialVersionUID = 1L;
    protected int key;

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
