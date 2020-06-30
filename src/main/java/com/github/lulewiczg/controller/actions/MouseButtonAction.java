package com.github.lulewiczg.controller.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract action for mouse button event.
 *
 * @author Grzegurz
 */
@Getter
@Setter
@AllArgsConstructor
public abstract class MouseButtonAction extends LoginRequiredAction {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return super.toString() + ", ";
    }

}
