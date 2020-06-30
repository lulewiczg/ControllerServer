package com.github.lulewiczg.controller.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lulewiczg.controller.server.ControllerServer;
import lombok.*;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Response sent to client after performing action.
 *
 * @author Grzegorz
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(of = "status")
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Status status;

    private String exception;

    private String exceptionStr;

    @JsonIgnore
    private transient Consumer<ControllerServer> callback;

    public Response(Status status, Exception exception) {
        this.status = status;
        this.exception = exception.getClass().getSimpleName();
        this.exceptionStr = exception.toString();
    }

    public Response(Status status, Consumer<ControllerServer> callback) {
        this.status = status;
        this.callback = callback;
    }

    public Response(Status status) {
        this.status = status;
    }


}
