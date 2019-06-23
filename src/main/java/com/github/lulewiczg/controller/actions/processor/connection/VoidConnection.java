package com.github.lulewiczg.controller.actions.processor.connection;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;

/**
 * Empty connection implementation.
 *
 * @author Grzegurz
 */
@Service("voidConnection")
public class VoidConnection implements ClientConnection {

    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    public void write(Response r) throws IOException {
        throw new IOException("Unexpected call");
    }

    @Override
    public Action getNext() throws Exception {
        throw new IOException("Unexpected call");
    }

}
