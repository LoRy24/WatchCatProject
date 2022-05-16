package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxiedPlayer;
import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.proxy.connection.CatProxiedPlayer;
import com.github.lory24.watchcatproxy.proxy.connection.SubServerInfo;
import com.github.lory24.watchcatproxy.proxy.logger.Logger;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyScheduler;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.proxy.logger.LoggerPrintStream;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    private LoggerPrintStream loggerPrintStream;

    // Server Objects
    private Logger logger;
    private ServerState state;
    private CatEventsManager eventsManager;
    private CatPluginsManager pluginsManager;

    // The scheduler
    private CatScheduler scheduler;

    // Exploit notification message timeout
    private final List<InetAddress> timeOutAddressesFromExMsg = new ArrayList<>();

    // Proxied core
    private final HashMap<String, ProxiedPlayer> proxiedPlayers = new HashMap<>();

    @Getter
    private HashMap<String, SubServerInfo> servers;
    private String defaultServerName;

    // Server icon file
    @Getter
    private File favIconFile;

    // Cooldown
    @Getter
    private final List<InetAddress> cooldownAddresses = new ArrayList<>();

    {
        this.state = ServerState.STARTING;
    }

    @Override
    public void run() {
        try {
            // Start the scheduler
            this.scheduler = new CatScheduler();

            // Load the logger and replace system defaults
            this.loggerPrintStream = new LoggerPrintStream(System.out);
            logger = new Logger(Logger.generateLoggerLogFile(), "WatchCat", this.loggerPrintStream);
            this.scheduler.runAsyncRepeat(null, () -> this.logger.saveLogger(), 60); // Save the logger every 60 ticks (3s)
            System.setErr(this.loggerPrintStream);
            getLogger().log(LogLevel.INFO, "Logger enabled!");

            // Load server properties
            this.serverProperties = new File("server-properties.json");
            ServerProperties.loadServerPropertiesFile(this.serverProperties, this);
            this.serverPropertiesJSONObject = new JSONObject(ServerProperties.loadFileContent(this.serverProperties));
            this.serverEnableTotalExploitCooldown = (boolean) ServerProperties.serverEnableExploitTotalCooldown.get(this.serverPropertiesJSONObject);
            getLogger().log(LogLevel.INFO, "Server properties loaded!");

            // Load the servers data
            this.servers = new HashMap<>();
            this.loadSubServers();
            this.defaultServerName = (String) ServerProperties.serverDefaultServer.get(this.serverPropertiesJSONObject);
            getLogger().log(LogLevel.INFO, "Proxy's sub servers have been loaded! Default server: " + this.defaultServerName);

            // Create the default icon file
            String serverIconName = (String) ServerProperties.serverIconName.get(this.serverPropertiesJSONObject);
            File defaultFavIconFile = new File("spycat.png");
            this.favIconFile = !serverIconName.equals("spycat.png")  ? new File(serverIconName) : defaultFavIconFile;
            this.loadDefaultFavIcon(defaultFavIconFile);
            getLogger().log(LogLevel.INFO, "Default proxy icon loaded!");

            // Instance the events manager
            this.eventsManager = new CatEventsManager();

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
                    if (this.blockedAddresses.contains(newConnection.getInetAddress()) || this.isInCooldown(newConnection)) {
                        newConnection.close();
                        continue;
                    }

                    // If the total exploit cooldown is enabled and the connection is in exploit cooldown,
                    // it'll be closed
                    cooldownCheck: {
                        if (this.serverEnableTotalExploitCooldown) {
                            if (!this.timeOutAddressesFromExMsg.contains(newConnection.getInetAddress()))
                                break cooldownCheck;
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
                InitialHandler initialHandler = new InitialHandler(conn, this);
                initialHandler.process();

                // Check if the client has been disconnected
                if (initialHandler.getState().equals(InitializationState.DISCONNECTED)) {
                    if (!conn.isClosed()) conn.close();
                    getLogger().log(LogLevel.WARNING, "Connection at " + conn.getInetAddress().getHostAddress() + " has disconnected: "
                            + initialHandler.getDisconnectReason());
                    return;
                }

                // Check if the client has been disconnected during the Login state
                if (initialHandler.getState().equals(InitializationState.DISCONNECTED_LOGIN)) {
                    if (!conn.isClosed()) conn.close();
                    getLogger().log(LogLevel.ERROR, "Connection at " + conn.getInetAddress().getHostAddress() + "has been disconnected during login state. The connection is now in cooldown");
                    this.putInCooldown(conn);
                    return;
                }

                // If the client is in the play status, it's time to start the proxiedConnection
                if (initialHandler.getState().equals(InitializationState.LOGIN)) {
                    // Encryption and compression are disabled
                    CatProxiedPlayer catProxiedPlayer = new CatProxiedPlayer(initialHandler.getLoginResult().username(), initialHandler.getLoginResult().uuid(), initialHandler.getLoginResult().onlineMode(), conn, this,
                            null, false);
                    catProxiedPlayer.proxiedConnection.runPacketsReplier(this.getServers().get(this.defaultServerName));
                    this.proxiedPlayers.put(initialHandler.getLoginResult().username(), catProxiedPlayer);
                }

            } catch (ReadExploitException e) { // Fix exploit
                try {
                    if (!checkExploitMessageTimeout(conn))
                        getLogger().log(LogLevel.WARNING, "The connection from " + conn.getInetAddress().getHostAddress() + " has been closed to prevent a server crash!");
                    conn.close();
                } catch (IOException ex) {
                    logError(ex.getMessage());
                    ex.printStackTrace();
                }
            }
            catch (IOException | BufferTypeException |
                     InvocationTargetException | IllegalAccessException e) {
                this.logError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private boolean checkExploitMessageTimeout(@NotNull Socket socket) {
        if (this.timeOutAddressesFromExMsg.contains(socket.getInetAddress())) return true;
        this.timeOutAddressesFromExMsg.add(socket.getInetAddress());
        getScheduler().runAsyncLater(null, () -> this.timeOutAddressesFromExMsg.remove(socket.getInetAddress()), 10);
        return false;
    }

    private void putInCooldown(@NotNull Socket socket) {
        if (!this.cooldownAddresses.contains(socket.getInetAddress())) this.cooldownAddresses.add(socket.getInetAddress());
        this.getScheduler().runAsyncLater(null, () ->
                this.cooldownAddresses.remove(socket.getInetAddress()), 200);
    }

    private boolean isInCooldown(@NotNull Socket socket) {
        return this.cooldownAddresses.contains(socket.getInetAddress());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadDefaultFavIcon(@NotNull File defaultFavIconFile) throws IOException {
        if (!defaultFavIconFile.exists()) {
            defaultFavIconFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(defaultFavIconFile);
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("spycat.png");
            fileOutputStream.write(Objects.requireNonNull(inputStream).readAllBytes());
            fileOutputStream.flush(); fileOutputStream.close();
        }
    }

    private void loadSubServers() {
        JSONArray serversJsonArray = this.serverPropertiesJSONObject.getJSONArray("servers");
        for (int i = 0; i < serversJsonArray.length(); i++) {
            String serverName = serversJsonArray.getJSONObject(i).getString("name"), serverAddress = serversJsonArray.getJSONObject(i).getString("address");
            int serverPort = serversJsonArray.getJSONObject(i).getInt("port");
            this.servers.put(serverName, new SubServerInfo(serverName, serverAddress, serverPort));
        }
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

    @Override
    public HashMap<String, ProxiedPlayer>  getProxiedPlayers() {
        return this.proxiedPlayers;
    }
}
