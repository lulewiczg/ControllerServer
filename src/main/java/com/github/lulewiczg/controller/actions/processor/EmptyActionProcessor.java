package com.github.lulewiczg.controller.actions.processor;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Response;

/**
 * Empty Action processor.
 *
 * @author Grzegurz
 */
@Service("emptyProcessor")
public class EmptyActionProcessor extends ActionProcessor {

    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    protected void write(Response r) throws IOException {
        throw new IOException("Unexpected call");
    }

    @Override
    protected Action getNext() throws Exception {
        throw new IOException("Unexpected call");
    }

}
