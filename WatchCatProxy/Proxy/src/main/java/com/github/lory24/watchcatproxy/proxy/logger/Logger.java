package com.github.lory24.watchcatproxy.proxy.logger;

import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.logging.ProxyLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

@SuppressWarnings({"unused", "deprecation"})
public class Logger extends ProxyLogger {
    private final File logFile;
    private final String loggerName;
    private final LoggerPrintStream customPrintStream;

    /**
     * The constructor for the logger object
     *
     * @param logFile           The file where all the log is going to be saved
     * @param loggerName        The name of the logger
     * @param customPrintStream The print stream where to write the messages
     */
    public Logger(File logFile, String loggerName, LoggerPrintStream customPrintStream) {
        this.logFile = logFile;
        this.loggerName = loggerName;
        this.customPrintStream = customPrintStream;
    }

    @Override
    public void saveLogger() {
        try {
            Files.writeString(Path.of(this.logFile.toURI()), customPrintStream.getBuffer().toString(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            this.log(LogLevel.ERROR, "Error while storing the log. Error: " + e.getMessage(), false);
        }
    }

    @Override
    public void log(LogLevel level, String message) {
        this.log(level, message, true);
    }

    @Override
    public void log(LogLevel level, String message, boolean store) {
        // Create the log string
        // Format: [hh:mm:ss] [prefix] message
        Date date = new Date();
        String logString = String.format("[ %02.0f:%02.0f:%02.0f ] [ %s - %s ] %s",
                (float) date.getHours(), (float) date.getMinutes(), (float) date.getSeconds(), loggerName, level, message
        );

        // Log the string
        this.customPrintStream.println(level.getLogColor() + logString);
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
                (float) date.getMonth() + 1, date.getYear() + 1900, (float) date.getHours(), (float) date.getMinutes(),
                (float) date.getSeconds()));
        logFile.createNewFile();
        return logFile;
    }
}
