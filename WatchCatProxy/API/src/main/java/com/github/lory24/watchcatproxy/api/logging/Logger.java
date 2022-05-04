package com.github.lory24.watchcatproxy.api.logging;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

@SuppressWarnings({"unused", "deprecation"})
public class Logger {

    /**
     * The file where all the log of this server session is going to be saved
     */
    private final File logFile;

    /**
     * The name for the logger. It's also used as prefix for the logger
     */
    private final String loggerName;

    /**
     * The current saved log file content. Used when writing data into the file
     */
    private String savedLogContent = "";

    /**
     * The constructor for the logger object
     * @param logFile The file where all the log is going to be saved
     * @param loggerName The name of the logger
     */
    public Logger(File logFile, String loggerName) {
        this.logFile = logFile;
        this.loggerName = loggerName;
    }

    /**
     * Function to write the log string into the log file.
     * @param logString The log string
     */
    private void storeLog(String logString) {
        savedLogContent += logString + "\n";
    }

    /**
     * Save the current log content into the log file.
     */
    public void saveLogger() {
        try {
            Files.writeString(Path.of(this.logFile.toURI()), savedLogContent, StandardOpenOption.APPEND);
            this.savedLogContent = "";
        } catch (IOException e) {
            this.log(LogLevel.ERROR, "Error while storing the log. Error: " + e.getMessage(), false);
        }
    }

    /**
     * Log a message
     * @param level The level of the log (ERROR, INFO etc.)
     * @param message The message to log
     */
    public void log(LogLevel level, String message) {
        this.log(level, message, true);
    }

    /**
     * Log a message
     * @param level The level of the log (ERROR, INFO etc.)
     * @param message The message to log
     * @param store If store the msg
     */
    public void log(LogLevel level, String message, boolean store) {
        // Create the log string
        // Format: [hh:mm:ss] [prefix] message
        Date date = new Date();
        String logString = String.format("[ %02.0f:%02.0f:%02.0f ] [ %s - %s ] %s",
                (float) date.getHours(), (float) date.getMinutes(), (float) date.getSeconds(), loggerName, level, message
        );

        // Log the string
        System.out.println(level.getLogColor() + logString);
        if (store) storeLog(logString);
    }

    /**
     * Creates the log file used by the logger when saving logs
     * @return The new file
     * @throws IOException If there is a problem when creating the file
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation", "UnusedReturnValue"})
    public static @NotNull File generateLoggerLogFile() throws IOException {
        // Create the folder file object
        final File logsFolder = new File("logs");
        if (!logsFolder.exists()) logsFolder.mkdir();
        // Create the log file
        Date date = new Date(); // used to create the file
        final File logFile = new File(logsFolder.getAbsolutePath(), String.format("LOG_%d-%02.0f-%d # %02.0f-%02.0f-%02.0f.txt", date.getDate(),
                (float) date.getMonth(), date.getYear() + 1900, (float) date.getHours(), (float) date.getMinutes(),
                (float) date.getSeconds()));
        logFile.createNewFile();
        return logFile;
    }
}
