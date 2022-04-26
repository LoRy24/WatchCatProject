package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class HandshakePacket implements Packet {
    @Getter private int protocolVersion;
    @Getter private String serverAddress;
    @Getter private int port;
    @Getter private int nextState;

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws ReadExploitException {
        buffer.readVarIntFromBuffer(); // Packet ID
        protocolVersion = buffer.readVarIntFromBuffer();
        serverAddress = buffer.readUTF8String();
        port = buffer.readUnsignedShort();
        nextState = buffer.readVarIntFromBuffer();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(0x00); // Packet ID
        buffer.writeVarIntToBuffer(protocolVersion);
        buffer.writeUTFString(serverAddress);
        buffer.writeUnsignedShort(port);
        buffer.writeVarIntToBuffer(nextState);
    }
}
