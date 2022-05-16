package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.EncryptionUtil;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ProxiedConnection {

    @Getter
    private final Socket socket;
    private boolean isStillConnected = true;

    private final CatProxiedPlayer catProxiedPlayer;
    private final WatchCatProxy proxy;

    // Replier task
    private Thread packetReplierTask;

    // Some connection info
    private final boolean onlineMode;
    private final EncryptionUtil encryptionUtil;

    @Getter @Setter
    private SubServerInfo connectedServer;

    // Choose if enable the encryption
    private final boolean enableCompression;

    @Getter @Setter
    private Socket subServerActiveConnection;

    public ProxiedConnection(Socket socket, CatProxiedPlayer catProxiedPlayer, WatchCatProxy proxy, boolean onlineMode, EncryptionUtil encryptionUtil, boolean enableCompression) {
        this.socket = socket;
        this.catProxiedPlayer = catProxiedPlayer;
        this.proxy = proxy;
        this.onlineMode = onlineMode;
        this.encryptionUtil = encryptionUtil;
        this.enableCompression = enableCompression;
    }

    public void runPacketsReplier(SubServerInfo defaultServer) throws IOException, BufferTypeException {
        this.setConnectedServer(defaultServer);
        this.catProxiedPlayer.connect(this.connectedServer);
        if (!isStillConnected) return;
        startConnectedCheckThread();
        this.packetReplierTask = new Thread(() -> {
            try {
                this.socket.setTcpNoDelay(true);
                while (isStillConnected) {

                }
                this.killConnection();
                subServerActiveConnection.close();
                this.proxy.getLogger().log(LogLevel.INFO, "[Proxy Alert -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()+ "] User " + catProxiedPlayer.getUsername()
                        + " has disconnected from the proxy.");
                this.proxy.getProxiedPlayers().remove(this.catProxiedPlayer.getUsername());
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.packetReplierTask.start();
    }

    public void startConnectedCheckThread() {
        while (isStillConnected) {
            try {
                if (socket.getInputStream().read() == -1) {
                    this.isStillConnected = false;
                    break;
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void sendPacketToClient(@NotNull Packet packet) throws BufferTypeException, IOException {
        PacketBuffer buffer = new PacketBuffer();
        packet.writeData(buffer);
        this.sendPacketDataToClient(buffer);
    }

    public void sendPacketDataToSubServer(PacketBuffer buffer) {

    }

    public void sendPacketDataToClient(PacketBuffer buffer) throws IOException,
            BufferTypeException {
        // Put all the data into a new packetBuffer
        PacketBuffer finalBuffer = createNewBuffer(buffer);
        if (onlineMode) {

        } else sendBuffer(finalBuffer, new DataOutputStream(this.socket.getOutputStream()));
    }

    private void sendBuffer(@NotNull PacketBuffer buffer, @NotNull DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(buffer.getBufferBytes());
    }

    /**
     * For now sends only uncompressed packets
     */
    @NotNull
    private PacketBuffer createNewBuffer(@NotNull PacketBuffer packetData) throws BufferTypeException {
        // Default uncompressed packet
        PacketBuffer finalBuffer = new PacketBuffer();
        finalBuffer.writeVarIntToBuffer(new VarInt(packetData.getBufferBytes().length));
        finalBuffer.writeBytes(packetData.getBufferBytes());
        return finalBuffer;
    }

    public void killConnection() {
        try {
            if (!this.getSocket().isClosed()) this.getSocket().close();
            if (this.subServerActiveConnection != null && !this.subServerActiveConnection.isClosed()) this.subServerActiveConnection.close();
            this.proxy.getProxiedPlayers().remove(this.catProxiedPlayer.getUsername());
            this.isStillConnected = false;
        } catch (IOException e) {
            proxy.getLogger().log(LogLevel.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
