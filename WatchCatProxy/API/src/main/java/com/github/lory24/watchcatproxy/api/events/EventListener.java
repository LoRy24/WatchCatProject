package com.github.lory24.watchcatproxy.api.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to determinate which function is used to execute an event
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {

    /**
     * This value is used to determinate if the function will be executed also if the event has been cancelled. It's
     * default value is true, so also when the event is cancelled, the function will be executed.
     * @return If the function ignore the cancelled state
     */
    boolean ignoreCancelled() default true;

    /**
     * The priority of the event listener function. The first function that will be executed is the one with the "HIGHEST"
     * priority, and the last to be executed is the "LOEWST" one. For a function that doesn't ignore the cancelled state,
     * the recommended choices are the "LOW" and the "LOWEST".
     * @return The event listener priority
     */
    EventPriority priority() default EventPriority.MONITOR;
}
