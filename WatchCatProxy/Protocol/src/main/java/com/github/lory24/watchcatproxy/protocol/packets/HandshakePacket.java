package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class HandshakePacket implements Packet {
    @Getter private VarInt protocolVersion;
    @Getter private String serverAddress;
    @Getter private int port;
    @Getter private VarInt nextState;

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws ReadExploitException, BufferTypeException {
        buffer.readVarIntFromBuffer(); // Packet ID
        protocolVersion = buffer.readVarIntFromBuffer();
        serverAddress = buffer.readUTF8String();
        port = buffer.readUnsignedShort();
        nextState = buffer.readVarIntFromBuffer();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(new VarInt(0x00)); // Packet ID
        buffer.writeVarIntToBuffer(protocolVersion);
        buffer.writeUTFString(serverAddress);
        buffer.writeUnsignedShort(port);
        buffer.writeVarIntToBuffer(nextState);
    }
}
