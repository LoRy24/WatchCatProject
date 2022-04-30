package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.events.defaults.HandshakeReceivedEvent;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import com.github.lory24.watchcatproxy.protocol.*;
import com.github.lory24.watchcatproxy.protocol.packets.HandshakePacket;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

    public void process()
            throws BufferTypeException, InvocationTargetException, IllegalAccessException,
            ReadExploitException {
        // Process the handshake state
        if (processHandshake() == -1) {
            return;
        }
    }

    private int processHandshake()
            throws InvocationTargetException,
            IllegalAccessException, ReadExploitException, BufferTypeException {
        // Read the handshake packet
        PacketBuffer handshakeBuffer = secureReadPacketBuffer();

        // Check if there was an error during handshake reading state
        if (handshakeBuffer == null) { disconnectNoPlayerMessage("Invalid Handshake procedure!"); return -1; }

        // Read the HandshakePacket and put the data into an object
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.readData(handshakeBuffer);
        this.handshakeResult = new HandshakeResult(handshakePacket.getProtocolVersion().toInteger(), handshakePacket.getServerAddress(), handshakePacket.getPort(),
                HandshakeResult.HandshakeNextState.convertIntegerToState(handshakePacket.getNextState().toInteger()));

        // Check for exploited packet
        ExploitUtils.checkBufferExploits(handshakeBuffer);

        // Fire the handshake event
        boolean cancelled = ProxyServer.getInstance().getEventsManager().fireEvent(HandshakeReceivedEvent.class,
                new HandshakeReceivedEvent(handshakeResult, this.socket.getInetAddress())
        );

        // If cancelled, exit with error code -1
        if (cancelled) {
            disconnectNoPlayerMessage("Handshake event cancelled");
            return -1;
        }

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
            ProxyServer.getInstance().getLogger().log(LogLevel.ERROR, "An error was occurred while disconnecting the socket! Error: " + e.getMessage());
        }
    }
}
