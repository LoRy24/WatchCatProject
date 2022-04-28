package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.logging.Logger;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.proxy.managers.CatEventsManager;
import com.github.lory24.watchcatproxy.proxy.managers.CatPluginsManager;
import lombok.Getter;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class WatchCatProxy extends CatProxyServer implements Runnable {

    // Server properties file
    @Getter
    private File serverProperties;

    // Server properties JSON
    @Getter
    private String serverPropertiesJSON;

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

    {
        this.state = ServerState.STARTING;
    }

    @Override
    public void run() {
        try {
            // Load the logger
            logger = new Logger(Logger.generateLoggerLogFile(), "WatchCat");
            getLogger().log(LogLevel.INFO, "Logger enabled!");

            // Load server properties
            this.serverProperties = new File("server-properties.json");
            this.loadServerPropertiesFile();
            this.serverPropertiesJSON = ServerProperties.loadFileContent(this.serverProperties);

            // Instance the events manager
            this.eventsManager = new CatEventsManager();
            getLogger().log(LogLevel.INFO, "Events manager has been instanced! Loading plugins...");

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadServerPropertiesFile() throws IOException {
        if (!serverProperties.exists()) {
            serverProperties.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(serverProperties);
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("server-properties.json");
            assert inputStream != null;
            fileOutputStream.write(inputStream.readAllBytes());
            fileOutputStream.flush();fileOutputStream.close();
        }
    }

    private void startServerSocket() {
        try {
            // Create the server
            this.serverSocket = new ServerSocket((int) ServerProperties.port.get(new JSONObject(this.serverPropertiesJSON)));
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
                    if (this.blockedAddresses.contains(newConnection.getInetAddress())) { newConnection.close(); continue; }

                    this.processConnection(newConnection);
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
            } catch (IOException | BufferTypeException |
                     InvocationTargetException | IllegalAccessException e) {
                this.logError(e.getMessage());
            }
            catch (ReadExploitException e) { // Fix exploit
                try {
                    getLogger().log(LogLevel.WARNING, "The connection from " + conn.getInetAddress().getHostAddress() + " has been closed to prevent a server crash!");
                    conn.close();
                } catch (IOException ex) {
                    logError(ex.getMessage());
                }
            }
        }).start();
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
        return (String) ServerProperties.port.get(new JSONObject(this.serverPropertiesJSON)
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
}
