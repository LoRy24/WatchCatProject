package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.VarIntUtils;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class InitialHandler {
    @Getter
    private InitializationState state = InitializationState.DISCONNECTED;

    // Private values
    private final Socket socket;

    // Disconnection values
    @Getter
    private String disconnectReason = "NO REASON PROVIDED";

    // API values
    @Getter
    private HandshakeResult handshakeResult;

    public InitialHandler(Socket socket) {
        this.socket = socket;
    }

    public void process() {
        // Process the handshake state
        if (processHandshake() == -1) {
            return;
        }
    }

    private int processHandshake() {
        // Read the handshake packet
        PacketBuffer handshakeBuffer = secureReadPacketBuffer();

        // Check if there was an error during handshake reading state
        if (handshakeBuffer == null) { disconnectNoPlayerMessage("Invalid Handshake procedure!"); return -1; }

        // Read the HandshakePacket and put the data into an object
        // TODO

        return 0;
    }

    /**
     * Fixed exploits: <br>
     * <ul>
     *     <li>Negative packet length</li>
     * </ul>
     *
     * NOTE: NOT COMPATIBLE WITH ENCRYPTION
     * @return The packet's data
     */
    public PacketBuffer secureReadPacketBuffer() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int length = VarIntUtils.readVarInt(dataInputStream);
            if (length <= 0) throw new Exception();
            return new PacketBuffer(dataInputStream.readNBytes(length));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void disconnectNoPlayerMessage(String reason) {
        try {
            this.state = InitializationState.DISCONNECTED;
            this.disconnectReason = reason;
            this.socket.close();
        } catch (IOException e) {
            CatProxyServer.getInstance().getLogger().log(LogLevel.ERROR, "An error was occurred while disconnecting the socket! Error: " + e.getMessage());
        }
    }
}
