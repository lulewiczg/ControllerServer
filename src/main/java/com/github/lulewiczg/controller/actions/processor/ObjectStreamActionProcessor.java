package com.github.lulewiczg.controller.actions.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.common.Response;

/**
 * Processor for Object Stream actions.
 *
 * @author Grzegurz
 */
public class ObjectStreamActionProcessor extends ActionProcessor {

    private ObjectInputStream in;

    private ObjectOutputStream out;

    public ObjectStreamActionProcessor(InputStream in, OutputStream out) {
        try {
            this.in = new ObjectInputStream(in);
            this.out = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#close()
     */
    @Override
    public void close() throws IOException {
        Common.close(in);
        Common.close(out);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#write(com.github.lulewiczg.controller.common.Response)
     */
    @Override
    protected void write(Response r) throws IOException {
        out.writeObject(r);
        out.flush();
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#getNext()
     */
    @Override
    protected Action getNext() throws Exception {
        return (Action) in.readObject();
    }

}
