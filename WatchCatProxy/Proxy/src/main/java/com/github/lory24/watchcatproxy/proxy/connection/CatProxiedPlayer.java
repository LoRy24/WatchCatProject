package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.ProxiedPlayer;
import com.github.lory24.watchcatproxy.protocol.EncryptionUtil;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class CatProxiedPlayer extends ProxiedPlayer {
    private final String username;
    private final UUID uuid;
    private final boolean isInOnlineMode;
    private final ProxiedConnection proxiedConnection;

    // Proxy instance
    private final WatchCatProxy proxy;

    public CatProxiedPlayer(String username,
                            UUID uuid,
                            boolean isInOnlineMode,
                            Socket socket,
                            WatchCatProxy proxy,
                            EncryptionUtil encryptionUtil) {
        this.username = username;
        this.uuid = uuid;
        this.isInOnlineMode = isInOnlineMode;
        this.proxiedConnection = new ProxiedConnection(socket, this, proxy, this.isInOnlineMode, encryptionUtil);
        this.proxy = proxy;
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
    public void disconnect(String reason) throws IOException {
        // SEND PLAY DISCONNECT PACKET
        this.proxiedConnection.killConnection();
        this.proxy.getProxiedPlayers().remove(this.getUsername());
    }
}
