package com.github.lulewiczg.controller.actions.processor.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.common.Response;

@Lazy
@Scope("prototype")
@Service("jsonConnection")
public class JSONClientConnection implements ClientConnection {

    public static final String DELIM = "||";
    private InputStream in;
    private OutputStream out;
    private ObjectMapper mapper = new ObjectMapper();
    private Scanner sc;

    @Autowired
    public JSONClientConnection(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        sc = new Scanner(in);
        sc.useDelimiter(DELIM);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#close()
     */
    @Override
    public void close() throws IOException {
        Common.close(in);
        Common.close(out);
        Common.close(sc);
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#write(com.github.lulewiczg.controller.common.Response)
     */
    @Override
    public void write(Response r) throws IOException {
        byte[] msg = (mapper.writeValueAsString(r) + DELIM).getBytes();
        out.write(msg);
        out.flush();
    }

    /**
     * @see com.github.lulewiczg.controller.actions.processor.ActionProcessor#getNext()
     */
    @Override
    public Action getNext() throws Exception {
        return mapper.readValue(sc.next(), Action.class);
    }
}
