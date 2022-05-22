package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.ProxiedPlayer;
import com.github.lory24.watchcatproxy.api.chatcomponent.TextChatComponent;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.EncryptionUtil;
import com.github.lory24.watchcatproxy.protocol.VersionsUtils;
import com.github.lory24.watchcatproxy.protocol.packets.PlayDisconnectPacket;
import com.github.lory24.watchcatproxy.protocol.packets.protocol_47.ChatMessagePacket;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

public class CatProxiedPlayer extends ProxiedPlayer {
    private final String username;
    private final UUID uuid;
    private final boolean isInOnlineMode;
    public final ProxiedConnection proxiedConnection;

    // Utils
    @Getter
    private final InetAddress address;

    // Handshake values
    @Getter
    private final HandshakeResult handshakeResult;

    // Proxy instance
    private final WatchCatProxy proxy;

    public CatProxiedPlayer(String username,
                            UUID uuid,
                            boolean isInOnlineMode,
                            Socket socket,
                            WatchCatProxy proxy,
                            EncryptionUtil encryptionUtil,
                            boolean compressionEnabled,
                            HandshakeResult handshakeResult)
            throws IOException {
        this.username = username;
        this.uuid = uuid;
        this.isInOnlineMode = isInOnlineMode;
        this.handshakeResult = handshakeResult;
        this.proxiedConnection = new ProxiedConnection(socket, this, proxy, this.isInOnlineMode, encryptionUtil, compressionEnabled);
        this.proxy = proxy;
        this.address = this.proxiedConnection.getSocket().getInetAddress();
    }

    public void connect(SubServerInfo serverInfo)
            throws IOException, BufferTypeException {
        SubServerInitialHandler subServerInitialHandler = new SubServerInitialHandler(this, serverInfo);
        subServerInitialHandler.initializeConnectionToSubServer(serverInfo);

        if (subServerInitialHandler.getConnInitialized() == -1) {
            this.disconnect("§cUnable to connect to the server " + serverInfo.name() + "!");
            this.proxy.getLogger().log(LogLevel.ERROR, "[InitialHandler : " + proxiedConnection.getSocket().getInetAddress().getHostAddress() + ":" + proxiedConnection.getSocket().getPort()
                    + " -> " + serverInfo.name() + "] Error while connecting to the server! Reason: " + subServerInitialHandler.getDisconnectReason());
            String playerMessage = "§cUnable to connect to the server " + serverInfo.name() + ": " +
                    subServerInitialHandler.getDisconnectReason();
            if (!this.proxiedConnection.connectedToAnyServer) this.disconnect(playerMessage);
            else this.sendMessage(playerMessage);
            return;
        }

        this.proxiedConnection.setSubServerActiveConnection(subServerInitialHandler.getServerConn());
        this.proxiedConnection.setConnectedServer(serverInfo);
        this.proxiedConnection.connectedToAnyServer = true;
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

    @Override
    public void sendMessage(String message) throws IOException {
        this.sendMessage(new TextChatComponent(message));
    }

    @Override
    public void sendMessage(@NotNull TextChatComponent message) throws IOException {
        try {
            this.proxiedConnection.sendPacketToClient(new ChatMessagePacket(message.toJson(), (byte) 0));
        } catch (BufferTypeException e) {
            e.printStackTrace();
        }
    }
}
