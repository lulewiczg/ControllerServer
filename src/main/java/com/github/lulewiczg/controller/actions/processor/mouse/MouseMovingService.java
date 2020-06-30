package com.github.lulewiczg.controller.actions.processor.mouse;

/**
 * Interface for mouse moving.
 *
 * @author Grzegurz
 */
public interface MouseMovingService {

    /**
     * Moves mouse by given coordinates
     *
     * @param dx x
     * @param dy y
     */
    void move(long dx, long dy);
}
