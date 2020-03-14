package com.github.lulewiczg.controller.common;

import java.io.Serializable;
import java.util.function.Consumer;

import com.github.lulewiczg.controller.server.ControllerServer;

import lombok.Getter;
import lombok.Setter;

/**
 * Response sent to client after performing action.
 *
 * @author Grzegorz
 */
@Getter
@Setter
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Status status;

    private Exception exception;

    private String exceptionStr;

    private transient Consumer<ControllerServer> callback;

    public Response(Status status, Exception exception) {
        this.status = status;
        this.exception = exception;
        this.exceptionStr = exception.toString();
    }

    public Response(Status status, Consumer<ControllerServer> callback) {
        this.status = status;
        this.callback = callback;
    }

    public Response(Status status) {
        this.status = status;
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

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Response)) {
            return false;
        }
        return ((Response) obj).status == status;
    }

}
