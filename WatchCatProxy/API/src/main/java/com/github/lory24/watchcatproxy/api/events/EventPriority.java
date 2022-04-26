package com.github.lory24.watchcatproxy.api.events;

import lombok.Getter;

/**
 * The event priority enum. This will be used by the events manager to determine the execution order of the listener
 * functions.
 */
public enum EventPriority {
    LOWEST(5),
    LOW(4),
    MONITOR(3),
    HIGH(2),
    HIGHEST(1),
    ;

    /**
     * The priority level number. Used to create the priority order by the server
     */
    @Getter
    private final int level;

    EventPriority(int level) {
        this.level = level;
    }
}
