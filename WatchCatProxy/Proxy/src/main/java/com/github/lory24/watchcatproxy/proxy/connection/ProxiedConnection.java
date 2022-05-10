package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.EncryptionUtil;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;

public class ProxiedConnection {

    @Getter
    private final Socket socket;

    private final CatProxiedPlayer catProxiedPlayer;
    private final WatchCatProxy proxy;

    // Replier task
    private Thread packetReplierTask;

    // Some connection info
    private final boolean onlineMode;
    private final EncryptionUtil encryptionUtil;

    @Getter @Setter
    private SubServerInfo connectedServer;

    private Socket subServerActiveConnection;

    public ProxiedConnection(Socket socket, CatProxiedPlayer catProxiedPlayer, WatchCatProxy proxy, boolean onlineMode, EncryptionUtil encryptionUtil) {
        this.socket = socket;
        this.catProxiedPlayer = catProxiedPlayer;
        this.proxy = proxy;
        this.onlineMode = onlineMode;
        this.encryptionUtil = encryptionUtil;
    }

    public void runPacketsReplier(SubServerInfo defaultServer) {
        this.setConnectedServer(defaultServer);
        this.packetReplierTask = new Thread(() -> {
            while (this.socket.isConnected()) {
                // PROCESS RECEIVED PACKETS and REPLY THE RECEIVED DATA
            }
            this.killConnection();
            Thread.currentThread().interrupt();
        });
        this.packetReplierTask.start();
    }

    public void sendPacketToClient(@NotNull Packet packet) throws BufferTypeException {
        PacketBuffer buffer = new PacketBuffer();
        packet.writeData(buffer);
        this.sendPacketDataToClient(buffer);
    }

    public void sendPacketDataToSubServer(PacketBuffer buffer) {

    }

    public void sendPacketDataToClient(PacketBuffer buffer) {
        // TODO IF THE CONNECTION IS IN ONLINE MODE, ENCRYPT THE BUFFER
        // TODO SEND THE DATA TO THE CLIENT
    }

    public void killConnection() {
        try {
            this.packetReplierTask.interrupt();
            if (!this.getSocket().isClosed()) this.getSocket().close();
            if (!this.subServerActiveConnection.isClosed()) this.subServerActiveConnection.close();
            this.proxy.getProxiedPlayers().remove(this.catProxiedPlayer.getUsername());
        } catch (IOException e) {
            proxy.getLogger().log(LogLevel.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
