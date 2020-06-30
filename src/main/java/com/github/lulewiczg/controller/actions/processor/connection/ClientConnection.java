package com.github.lulewiczg.controller.actions.processor.connection;

import java.io.Closeable;
import java.io.IOException;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;

/**
 * Interface for communication with client.
 *
 * @author Grzegurz
 */
public interface ClientConnection extends Closeable {

    /**
     * Writes response to output stream.
     */
    void write(Response r) throws IOException;

    /**
     * Reads next action.
     *
     * @return action action
     * @throws Exception the Exception
     */
    Action getNext() throws Exception;

}
