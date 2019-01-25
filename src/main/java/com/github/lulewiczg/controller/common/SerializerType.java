package com.github.lulewiczg.controller.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiFunction;

import com.github.lulewiczg.controller.actions.processor.ActionProcessor;
import com.github.lulewiczg.controller.actions.processor.ObjectStreamActionProcessor;

/**
 * Enum for serializer type.
 *
 * @author Grzegurz
 */
public enum SerializerType {
    OBJECT_STREAM("Object stream", (in, out) -> new ObjectStreamActionProcessor(in, out));

    private String label;
    private BiFunction<InputStream, OutputStream, ActionProcessor> supplier;

    /**
     * Gets serializer.
     *
     * @param in
     *            input
     * @param out
     *            output
     * @return serializer
     */
    public ActionProcessor getSerializer(InputStream in, OutputStream out) {
        return supplier.apply(in, out);
    }

    private SerializerType(String label, BiFunction<InputStream, OutputStream, ActionProcessor> supplier) {
        this.label = label;
        this.supplier = supplier;
    }

    public String getLabel() {
        return label;
    }

}
