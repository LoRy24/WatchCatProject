package com.github.lory24.watchcatproxy.api.logging;

import lombok.Getter;

@SuppressWarnings("unused")
public enum LogLevel {

    /**
     * When the log is an error (like a stack trace)
     * Color: RED
     */
    ERROR("\u001B[31m"),

    /**
     * When the log is an information (like a normal message)
     * Color: BLUE
     */
    INFO("\u001B[36m"),

    /**
     * When the log is a warning (for example something has not loaded correctly)
     * Color: YELLOW
     */
    WARNING("\u001B[33m"),

    /**
     * When the log is a success message (like proxy loaded and started)
     */
    SUCCESS("\u001B[32m"),
    ;

    /**
     * The color for the log message.
     */
    @Getter
    private final String logColor;

    /**
     * Constructor for the LogLevel enum.
     * @param logColor The color
     */
    LogLevel(String logColor) {
        this.logColor = logColor;
    }
}
