package com.github.lulewiczg.controller.actions.processor.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.common.Response;

/**
 * Communication with client using serialization..
 *
 * @author Grzegurz
 */
@Lazy
@Primary
@Scope("prototype")
@Service("objectStreamConnection")
public class ObjectStreamClientConnection implements ClientConnection {

    private ObjectInputStream in;
    private ObjectOutputStream out;

    @Autowired
    public ObjectStreamClientConnection(InputStream in, OutputStream out) {
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
    public void write(Response r) throws IOException {
        out.writeObject(r);
        out.flush();
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#getNext()
     */
    @Override
    public Action getNext() throws Exception {
        return (Action) in.readObject();
    }
}
