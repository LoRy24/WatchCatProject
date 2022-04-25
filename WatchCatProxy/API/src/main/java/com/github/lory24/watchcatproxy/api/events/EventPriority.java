package com.github.lory24.watchcatproxy.api.events;

/**
 * The event priority enum. This will be used by the events manager to determine the execution order of the listener
 * functions.
 */
public enum EventPriority {
    LOWEST,
    LOW,
    MONITOR,
    HIGH,
    HIGHEST
}
