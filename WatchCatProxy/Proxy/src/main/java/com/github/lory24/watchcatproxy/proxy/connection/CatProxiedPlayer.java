package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.ProxiedPlayer;
import com.github.lory24.watchcatproxy.api.chatcomponent.TextChatComponent;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.EncryptionUtil;
import com.github.lory24.watchcatproxy.protocol.VersionsUtils;
import com.github.lory24.watchcatproxy.protocol.packets.PlayDisconnectPacket;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class CatProxiedPlayer extends ProxiedPlayer {
    private final String username;
    private final UUID uuid;
    private final boolean isInOnlineMode;
    public final ProxiedConnection proxiedConnection;

    // Proxy instance
    private final WatchCatProxy proxy;

    public CatProxiedPlayer(String username,
                            UUID uuid,
                            boolean isInOnlineMode,
                            Socket socket,
                            WatchCatProxy proxy,
                            EncryptionUtil encryptionUtil,
                            boolean compressionEnabled)
            throws IOException {
        this.username = username;
        this.uuid = uuid;
        this.isInOnlineMode = isInOnlineMode;
        this.proxiedConnection = new ProxiedConnection(socket, this, proxy, this.isInOnlineMode, encryptionUtil, compressionEnabled);
        this.proxy = proxy;
    }

    public void connect(SubServerInfo serverInfo)
            throws IOException, BufferTypeException {
        SubServerInitialHandler subServerInitialHandler = new SubServerInitialHandler(this);
        subServerInitialHandler.initializeConnectionToSubServer(serverInfo);

        if (subServerInitialHandler.getServerConn() == null) {
            this.disconnect("§cUnable to connect to the server " + serverInfo.name() + "!");
            this.proxy.getLogger().log(LogLevel.ERROR, "[InitialHandler : " + proxiedConnection.getSocket().getInetAddress().getHostAddress() + ":" + proxiedConnection.getSocket().getPort()
                    + " -> " + serverInfo.name() + "] Error while connecting to the server! Disconnecting the player...");
            return;
        }

        this.proxiedConnection.setSubServerActiveConnection(subServerInitialHandler.getServerConn());
        this.proxiedConnection.setConnectedServer(serverInfo);
    }

    @Deprecated
    public void connect(String name, String host, int port) throws IOException,
            BufferTypeException {
        this.connect(new SubServerInfo(name, host, port));
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean isInOnlineMode() {
        return this.isInOnlineMode;
    }

    @Override
    public void disconnect(String reason) throws IOException, BufferTypeException {
        this.proxiedConnection.sendPacketToClient(new PlayDisconnectPacket(VersionsUtils.getPlayDisconnectPacketID(47),
                new TextChatComponent(reason).buildTextChatComponent()));
        this.proxiedConnection.killConnection();
    }
}
