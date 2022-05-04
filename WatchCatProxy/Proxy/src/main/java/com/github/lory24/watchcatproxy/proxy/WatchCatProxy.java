package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.logging.Logger;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyScheduler;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class WatchCatProxy extends ProxyServer implements Runnable {

    // Server properties file
    @Getter
    private File serverProperties;

    // Configuration values
    @Getter
    private boolean serverEnableTotalExploitCooldown;

    // Server properties JSON
    @Getter
    private JSONObject serverPropertiesJSONObject;

    // Security stuff
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<InetAddress> blockedAddresses = new ArrayList<>();

    // Internal server features
    private ServerSocket serverSocket;

    // Server Objects
    private Logger logger;
    private ServerState state;
    private CatEventsManager eventsManager;
    private CatPluginsManager pluginsManager;

    // The scheduler
    private CatScheduler scheduler;

    // Exploit notification message timeout
    private final List<InetAddress> timeOutAddressesFromExMsg = new ArrayList<>();

    {
        this.state = ServerState.STARTING;
    }

    @Override
    public void run() {
        try {
            // Start the scheduler
            this.scheduler = new CatScheduler();

            // Load the logger
            logger = new Logger(Logger.generateLoggerLogFile(), "WatchCat");
            this.scheduler.runAsyncRepeat(null, () -> this.logger.saveLogger(), 60); // Save the logger every 60 ticks (3s)
            getLogger().log(LogLevel.INFO, "Logger enabled!");

            // Load server properties
            this.serverProperties = new File("server-properties.json");
            ServerProperties.loadServerPropertiesFile(this.serverProperties, this);
            this.serverPropertiesJSONObject = new JSONObject(ServerProperties.loadFileContent(this.serverProperties));
            this.serverEnableTotalExploitCooldown = (boolean) ServerProperties.serverEnableExploitTotalCooldown.get(this.serverPropertiesJSONObject);

            // Instance the events manager
            this.eventsManager = new CatEventsManager();
            getLogger().log(LogLevel.INFO, "Events manager has been loaded! Loading plugins, wait.");

            // Load the plugins
            this.pluginsManager = new CatPluginsManager(this);
            this.pluginsManager.setup();

            // Start the server-socket and finish starting the server
            startServerSocket();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException | ClassNotFoundException e) { // Logger managed exception
            logError(e.getMessage());
        }
    }

    private void startServerSocket() {
        try {
            // Create the server
            this.serverSocket = new ServerSocket((int) ServerProperties.port.get(this.serverPropertiesJSONObject));
            // Start listening
            listening();
        } catch (IOException e) {
            this.logError(e.getMessage());
        }
    }

    private void listening() {
        getLogger().log(LogLevel.SUCCESS, "NET server started! Listening...");
        this.state = ServerState.STARTED;
        new Thread(() -> {
            while (this.state != ServerState.STOPPING) {
                try {
                    // Accept the new connection
                    Socket newConnection = this.serverSocket.accept();

                    // Check if the connection is blocked
                    if (this.blockedAddresses.contains(newConnection.getInetAddress())) {
                        newConnection.close();
                        continue;
                    }

                    // If the total exploit cooldown is enabled and the connection is in exploit cooldown,
                    // it'll be closed
                    cooldownCheck: {
                        if (this.serverEnableTotalExploitCooldown) {
                            if (!this.timeOutAddressesFromExMsg.contains(newConnection.getInetAddress())) break cooldownCheck;
                            newConnection.close();
                        }
                    }

                    if(!newConnection.isClosed()) this.processConnection(newConnection);
                } catch (IOException e) {
                    this.logError(e.getMessage());
                }
            }
        }).start();
    }

    private void processConnection(Socket conn) {
        new Thread(() -> {
            try {
                // Initialize the client
                InitialHandler initialHandler = new InitialHandler(conn);
                initialHandler.process();

                // Check if the client has been disconnected
                if (initialHandler.getState().equals(InitializationState.DISCONNECTED)) {
                    if (!conn.isClosed()) conn.close();
                    getLogger().log(LogLevel.WARNING, "Connection at " + conn.getInetAddress().getHostAddress() + " has disconnected: "
                            + initialHandler.getDisconnectReason());
                }

                conn.close();
            } catch (ReadExploitException e) { // Fix exploit
                try {
                    if (!checkExploitMessageTimeout(conn))
                        getLogger().log(LogLevel.WARNING, "The connection from " + conn.getInetAddress().getHostAddress() + " has been closed to prevent a server crash!");
                    conn.close();
                } catch (IOException ex) {
                    logError(ex.getMessage());
                }
            }
            catch (IOException | BufferTypeException |
                     InvocationTargetException | IllegalAccessException e) {
                this.logError(e.getMessage());
            }
        }).start();
    }

    private boolean checkExploitMessageTimeout(@NotNull Socket socket) {
        if (this.timeOutAddressesFromExMsg.contains(socket.getInetAddress())) return true;
        this.timeOutAddressesFromExMsg.add(socket.getInetAddress());
        getScheduler().runAsyncLater(null, () -> this.timeOutAddressesFromExMsg.remove(socket.getInetAddress()), 10);
        return false;
    }

    private void logError(String message) {
        getLogger().log(LogLevel.ERROR, "An error has occurred: " + message);
    }

    @Override
    public EventsManager getEventsManager() {
        return this.eventsManager;
    }

    @Override
    public String getVersion() {
        return (String) ServerProperties.port.get(this.serverPropertiesJSONObject
                .getJSONObject("version"));
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public PluginsManager getPluginsManager() {
        return this.pluginsManager;
    }

    @Override
    public ProxyScheduler getScheduler() {
        return this.scheduler;
    }
}
