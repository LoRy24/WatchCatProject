package com.github.lory24.watchcatproxy.api.plugin;

public class PluginNotLoadedException extends Exception {

    /**
     * Error fired when a plugin files cannot be loaded into the server JVM
     * @param message The error message
     */
    public PluginNotLoadedException(String message) {
        super(message);
    }
}
