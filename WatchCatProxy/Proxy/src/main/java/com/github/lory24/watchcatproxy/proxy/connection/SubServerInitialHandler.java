package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import com.github.lory24.watchcatproxy.protocol.*;
import com.github.lory24.watchcatproxy.protocol.packets.HandshakePacket;
import com.github.lory24.watchcatproxy.protocol.packets.LoginStartPacket;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import com.github.lory24.watchcatproxy.proxy.InitializationState;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SubServerInitialHandler {

    @Getter @Setter
    private InitializationState state;

    @Getter
    private Socket serverConn;
    @Getter private int connInitialized = -1;

    // Player infos
    private final CatProxiedPlayer catProxiedPlayer;

    // Server infos
    private final SubServerInfo subServerInfo;

    // Connection internal
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    // Disconnect details
    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private String disconnectReason = "The server is offline.";

    public SubServerInitialHandler(CatProxiedPlayer catProxiedPlayer, SubServerInfo subServerInfo) {
        this.catProxiedPlayer = catProxiedPlayer;
        this.subServerInfo = subServerInfo;
    }

    public void initializeConnectionToSubServer(SubServerInfo subServerInfo) throws IOException {
        // Connect to the server
        this.serverConn = new Socket();
        this.serverConn.connect(new InetSocketAddress(this.subServerInfo.host(), this.subServerInfo.port()));
        this.dataOutputStream = new DataOutputStream(serverConn.getOutputStream());
        this.dataInputStream = new DataInputStream(serverConn.getInputStream());


        int handshake = processHandshake(), login = processLogin();
        // Send handshake and the login state
        if (handshake == -1 || login == -1) {
            this.state = InitializationState.DISCONNECTED;
            this.serverConn = null;
            return;
        }
    }

    public int processHandshake() {
        int resultCode = -1;

        try {
            // Send the handshake packet
            String newHost = "127.0.0.1 \00" + catProxiedPlayer.getAddress().getHostAddress() + "\00" + catProxiedPlayer.getUUID();

            HandshakePacket handshakePacket = new HandshakePacket(new VarInt(catProxiedPlayer.getHandshakeResult().protocolVersion()), newHost, serverConn.getPort(),
                    new VarInt(HandshakeResult.HandshakeNextState.LOGIN.getValue()));
            this.sendNormalPacket(handshakePacket);

            if (!this.serverConn.isClosed()) resultCode = 0;
        } catch (IOException | BufferTypeException e) {
            e.printStackTrace();
        }

        return resultCode;
    }

    public int processLogin() {
        int result = -1;
        try {
            // Send login request
            this.sendNormalPacket(new LoginStartPacket(this.catProxiedPlayer.getUsername()));
            PacketBuffer receivedPacketBufferA = new PacketBuffer(), copiedPacketBufferA = new PacketBuffer(receivedPacketBufferA.getBufferBytes());
            if (copiedPacketBufferA.readVarIntFromBuffer().intValue() == 0x02) { // Check if the received packet is a disconnect packet

            }
            // Check for Encryption packets - Disconnect the client
            // Check for Compression packet - Disconnect
            // Read the login success packet
        } catch (IOException | BufferTypeException | ReadExploitException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void sendNormalPacket(@NotNull Packet packet) throws IOException, BufferTypeException {
        PacketBuffer outBuffer = new PacketBuffer();
        packet.writeData(outBuffer);
        this.dataOutputStream.write(new VarInt(outBuffer.getBufferBytes().length).varIntBuffer.getBufferBytes());
        this.dataOutputStream.write(outBuffer.getBufferBytes());
    }

    public PacketBuffer secureReadPacketBuffer() throws IOException, BufferTypeException {
        VarInt length = VarIntUtil.readVarInt(dataInputStream);
        if (length.intValue() <= 0) return null;
        return new PacketBuffer(dataInputStream.readNBytes(length.intValue()));
    }
}
