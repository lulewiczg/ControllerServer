package com.github.lulewiczg.controller.actions.processor.connection;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.common.Response;
import com.github.lulewiczg.controller.exception.ConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Object stream implementation of connection.
 */
@Lazy
@Scope("prototype")
@Component("objectStreamConnection")
public class ObjectStreamClientConnection implements ClientConnection {

    public static final String NAME = "objectStreamConnection";
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    @Autowired
    public ObjectStreamClientConnection(InputStream in, OutputStream out) {
        try {
            this.in = new ObjectInputStream(in);
            this.out = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#close()
     */
    @Override
    public void close() {
        Common.close(in);
        Common.close(out);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.connection.ClientConnection#write(com.github.lulewiczg.controller.common.Response)
     */
    @Override
    public void write(Response r) throws IOException {
        out.writeObject(r);
        out.flush();
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.connection.ClientConnection#getNext()
     */
    @Override
    public Action getNext() throws Exception {
        return (Action) in.readObject();
    }
}
