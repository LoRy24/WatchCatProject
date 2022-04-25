package com.github.lory24.watchcatproxy.api.logging;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class Logger implements Runnable {

    /**
     * The log object. Used to store pending logs into the logs queue
     *
     * @param message The log message
     * @param level   The log level
     * @param store   If store the log
     */
    public record Log(@Getter String message, @Getter LogLevel level, @Getter boolean store) {}

    /**
     * The file where all the log of this server session is going to be saved
     */
    private final File logFile;

    /**
     * The name for the logger. It's also used as prefix for the logger
     */
    private final String loggerName;

    /**
     * The pending logs queue
     */
    private final List<Log> pendingQueue = new ArrayList<>();

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
        Thread loggerThread = new Thread(this);
        loggerThread.setPriority(2);
        loggerThread.start();
    }

    /**
     * Run the logger runnable. This is going to send all the logs whenever there is a new
     * one in the queue
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        // Run this loop while the thread is running
        while (!Thread.currentThread().isInterrupted()) {
            if (pendingQueue.size() == 0) continue;

            // Create the log string
            // Format: [hh:mm:ss] [prefix] message
            Date date = new Date();
            Log logObject = pendingQueue.get(0);
            String logString = String.format("[ %02.0f:%02.0f:%02.0f ] [ %s - %s ] %s",
                    (float) date.getHours(), (float) date.getMinutes(), (float) date.getSeconds(),
                    loggerName,
                    logObject.level,
                    logObject.message
            );

            // Log the string
            System.out.println(logObject.level().getLogColor() + logString);
            if (logObject.store()) storeLog(logString);

            // Remove obj from queue
            pendingQueue.remove(0);
        }
    }

    /**
     * Function to write the log string into the log file.
     * @param logString The log string
     */
    private void storeLog(String logString) {
        try {
            savedLogContent += logString + "\n";
            FileWriter writer = new FileWriter(logFile);
            writer.write(savedLogContent);
            writer.close();
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
        Log errorLog = new Log(message, level, true);
        this.pendingQueue.add(errorLog);
    }

    /**
     * Log a message
     * @param level The level of the log (ERROR, INFO etc.)
     * @param message The message to log
     * @param store If store the msg
     */
    public void log(LogLevel level, String message, boolean store) {
        Log errorLog = new Log(message, level, store);
        this.pendingQueue.add(errorLog);
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
