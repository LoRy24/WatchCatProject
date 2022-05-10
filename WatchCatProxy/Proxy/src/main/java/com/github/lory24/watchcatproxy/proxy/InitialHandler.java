package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ChatComponent;
import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.events.defaults.HandshakeReceivedEvent;
import com.github.lory24.watchcatproxy.api.events.defaults.ServerListPingEvent;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import com.github.lory24.watchcatproxy.api.status.ProxyServerStatus;
import com.github.lory24.watchcatproxy.protocol.*;
import com.github.lory24.watchcatproxy.protocol.packets.HandshakePacket;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import com.github.lory24.watchcatproxy.protocol.packets.StatusPingPongPacket;
import com.github.lory24.watchcatproxy.protocol.packets.StatusResponsePacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public class InitialHandler {
    @Getter
    private InitializationState state = InitializationState.DISCONNECTED;

    // Private values
    private final Socket socket;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final DataOutputStream dataOutputStream;

    // Disconnection values
    @Getter
    private String disconnectReason = "NO REASON PROVIDED";

    // API values
    @Getter
    private HandshakeResult handshakeResult;

    // References
    private final WatchCatProxy proxy;

    public InitialHandler(@NotNull Socket socket, WatchCatProxy proxy) throws IOException {
        this.socket = socket;
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.proxy = proxy;
    }

    @SuppressWarnings("UnnecessaryBreak")
    public void process()
            throws BufferTypeException, InvocationTargetException, IllegalAccessException,
            ReadExploitException, IOException {
        // Process the handshake state
        if (processHandshakeReceive() == -1) {
            return;
        }

        // Process the result
        switch (handshakeResult.getNextState()) {

            case STATUS -> {
                processStatus();
                break;
            }


            case LOGIN -> {

            }

            case WEBPANEL_ACTION -> {

            }

            default -> this.disconnectNoPlayerMessage("Received an unsupported handshake next-state");
        }
    }

    private int processHandshakeReceive()
            throws InvocationTargetException, IllegalAccessException, ReadExploitException, BufferTypeException, IOException {
        // Read the handshake packet
        PacketBuffer handshakeBuffer = secureReadPacketBuffer();

        // Check if there was an error during handshake reading state
        if (handshakeBuffer == null || handshakeBuffer.getBufferBytes().length > 20) {
            disconnectNoPlayerMessage("Invalid Handshake procedure!");
            return -1;
        }

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

    private void processStatus() throws IOException, BufferTypeException, InvocationTargetException, IllegalAccessException, ReadExploitException {

        // Read the status request packet buffer, not necessary to read the packet
        PacketBuffer statusRequestPacketBuffer = this.secureReadPacketBuffer();

        // Check for problems
        if (statusRequestPacketBuffer == null || statusRequestPacketBuffer.getBufferBytes().length > 1) {
            disconnectNoPlayerMessage("Invalid status procedure!");
        }

        // TODO optimize this
        // Process the event before sending the status
        ProxyServerStatus defaultProxyServerStatus = new ProxyServerStatus(
                // Version
                new ProxyServerStatus.StatusVersion(
                        (String) ServerProperties.serverVersion.get(this.proxy.getServerPropertiesJSONObject().getJSONObject("version")), (int) ServerProperties.protocolVersion.get(
                                this.proxy.getServerPropertiesJSONObject().getJSONObject("version"))
                ),
                // Players
                new ProxyServerStatus.StatusPlayers(
                        (int) ServerProperties.maxPlayers.get(this.proxy.getServerPropertiesJSONObject().getJSONObject("players")), ((boolean) ServerProperties.fakeOnlineEnabled.get(this.proxy.getServerPropertiesJSONObject()
                        .getJSONObject("players").getJSONObject("fakeOnline"))) ? (int) ServerProperties.fakeOnlineValue.get(this.proxy.getServerPropertiesJSONObject()
                        .getJSONObject("players").getJSONObject("fakeOnline")) : this.proxy.getProxiedPlayers().size(),
                        ((boolean) ServerProperties.enableCustomSample.get(this.proxy.getServerPropertiesJSONObject().getJSONObject("players"))) ? ProxyServerStatus.StatusPlayers.buildSampleFromString(((String) ServerProperties.sample.get(this.proxy.getServerPropertiesJSONObject()
                                .getJSONObject("players"))).replace("&", "\u00a7")) : ProxyServerStatus.StatusPlayers.buildSampleFromPlayersHashMap(this.proxy.getProxiedPlayers())
                ),
                // Description object
                new ChatComponent.TextChatComponent(((String) ServerProperties.serverMessageOfTheDay.get(proxy.getServerPropertiesJSONObject()))
                        .replace("&", "\u00a7")),
                // The icon
                new ProxyServerStatus.FavIcon(this.proxy.getFavIconFile())
        );

        // Fire the event
        boolean cancelled = ProxyServer.getInstance().getEventsManager().fireEvent(ServerListPingEvent.class,
                new ServerListPingEvent(socket.getInetAddress(), defaultProxyServerStatus)
        );

        // If the event is cancelled, it will close the connection
        if (cancelled) {
            this.socket.close();
            this.state = InitializationState.STATUS;
            return;
        }

        // Send the status response packet
        StatusResponsePacket statusResponsePacket = new StatusResponsePacket(defaultProxyServerStatus.buildJSON());
        sendInitializationSimplePacket(statusResponsePacket);

        // Read the ping packet and send the pong packet
        PacketBuffer pingPacketBuffer = secureReadPacketBuffer();

        // Check for problems
        if (pingPacketBuffer == null) {
            disconnectNoPlayerMessage("Invalid status procedure!");
            return;
        }

        // Read the ping packet
        StatusPingPongPacket statusPingPongPacket = new StatusPingPongPacket();
        statusPingPongPacket.readData(pingPacketBuffer);

        // Send it back
        sendInitializationSimplePacket(statusPingPongPacket);

        this.state = InitializationState.STATUS;
        this.socket.close();

        // Notify the status success
        this.proxy.getLogger().log(LogLevel.INFO, "[InitialHandler -> " + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort() +
                "] 'ServerListPing' connection has been managed.");
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
    public PacketBuffer secureReadPacketBuffer() throws IOException, BufferTypeException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        VarInt length = VarIntUtil.readVarInt(dataInputStream);
        if (length.toInteger() <= 0) return null;
        return new PacketBuffer(dataInputStream.readNBytes(length.toInteger()));
    }

    public void sendInitializationSimplePacket(@NotNull Packet packet)
            throws BufferTypeException, IOException {
        PacketBuffer packetBuffer = new PacketBuffer();
        packet.writeData(packetBuffer);
        this.dataOutputStream.write(new VarInt(packetBuffer.getBufferBytes().length).varIntBuffer.getBufferBytes());
        this.dataOutputStream.write(packetBuffer.getBufferBytes());
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
