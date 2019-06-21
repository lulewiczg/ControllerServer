package com.github.lulewiczg.controller.common;

import java.io.Serializable;
import java.util.function.Consumer;

import com.github.lulewiczg.controller.server.ControllerServer;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Status status;

    private Exception exception;

    private transient Consumer<ControllerServer> callback;

    public Response(Status status, Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public Response(Status status, Consumer<ControllerServer> callback) {
        this.status = status;
        this.callback = callback;
    }

    public Response(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Response: ").append(status);
        if (exception != null) {
            str.append(", cause:\n").append(exception.toString());
            for (StackTraceElement element : exception.getStackTrace()) {
                str.append(element.toString());
                str.append("\n");
            }
        }
        return str.toString();
    }

    public Consumer<ControllerServer> getCallback() {
        return callback;
    }

    public void setEvent(Consumer<ControllerServer> callback) {
        this.callback = callback;
    }

}
