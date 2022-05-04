package com.github.lory24.watchcatproxy.api.logging;

/**
 * The logger abstract class.
 */
public abstract class ProxyLogger {

    /**
     * Save the unsaved messages into the log file.
     */
    public abstract void saveLogger();

    /**
     * Log a message
     * @param level The level of the log (ERROR, INFO etc.)
     * @param message The message to log
     */
    public abstract void log(LogLevel level, String message);

    /**
     * Log a message
     * @param level The level of the log (ERROR, INFO etc.)
     * @param message The message to log
     * @param store If store the msg
     */
    public abstract void log(LogLevel level, String message, boolean store);
}
