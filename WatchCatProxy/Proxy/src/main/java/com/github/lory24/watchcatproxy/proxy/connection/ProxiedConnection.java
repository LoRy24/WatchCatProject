package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.protocol.*;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ProxiedConnection {

    @Getter
    private final Socket socket;
    private final DataInputStream clientDataInputStream;
    private final DataOutputStream clientDataOutputStream;
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

    public ProxiedConnection(@NotNull Socket socket, CatProxiedPlayer catProxiedPlayer, WatchCatProxy proxy, boolean onlineMode, EncryptionUtil encryptionUtil,
                             boolean enableCompression) throws IOException {
        this.socket = socket;
        this.catProxiedPlayer = catProxiedPlayer;
        this.proxy = proxy;
        this.onlineMode = onlineMode;
        this.encryptionUtil = encryptionUtil;
        this.enableCompression = enableCompression;
        this.clientDataInputStream = new DataInputStream(socket.getInputStream());
        this.clientDataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void runPacketsReplier(SubServerInfo defaultServer) throws IOException, BufferTypeException {
        this.setConnectedServer(defaultServer);
        this.catProxiedPlayer.connect(this.connectedServer);
        this.packetReplierTask = new Thread(() -> {
            while (isStillConnected) {

                /*
                 * Reply the packet received from the client.
                 * CLIENT -> PROXY -> SERVER
                 */
                PacketBuffer receivedPacketFromClient = readClientIncomingPacketBuffer();
                // If the received buffer is null, close the connection
                if (receivedPacketFromClient == null) {
                    this.isStillConnected = false;
                    break;
                }
                this.sendPacketDataToSubServer(receivedPacketFromClient);
            }
            this.killConnection();
            this.proxy.getLogger().log(LogLevel.INFO, "[Proxy Alert -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()+ "] User " + catProxiedPlayer.getUsername()
                    + " has disconnected from the proxy.");
            this.proxy.getProxiedPlayers().remove(this.catProxiedPlayer.getUsername());
            Thread.currentThread().interrupt();
        });
        this.packetReplierTask.start();
    }

    @Nullable
    private PacketBuffer readClientIncomingPacketBuffer() {
        try {
            VarInt packetLength = VarIntUtil.readVarInt(this.clientDataInputStream);
            if (packetLength.intValue() == -1) return null;
            return new PacketBuffer(this.clientDataInputStream.readNBytes(packetLength.intValue()));
        } catch (IOException | BufferTypeException e) {
            return null;
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

        } else sendBuffer(finalBuffer, this.clientDataOutputStream);
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
