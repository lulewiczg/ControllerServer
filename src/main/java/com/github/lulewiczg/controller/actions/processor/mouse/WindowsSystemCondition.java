package com.github.lulewiczg.controller.actions.processor.mouse;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Tests if OS is Windows.
 * 
 * @author Grzegurz
 */
public class WindowsSystemCondition implements Condition {

    /**
     * @see org.springframework.context.annotation.Condition#matches(ConditionContext, AnnotatedTypeMetadata)
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
