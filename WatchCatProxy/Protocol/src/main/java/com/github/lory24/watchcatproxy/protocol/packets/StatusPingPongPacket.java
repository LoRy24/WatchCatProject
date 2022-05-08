package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class StatusPingPongPacket implements Packet {

    @Getter
    private long payload;

    public StatusPingPongPacket(long payload) {
        this.payload = payload;
    }

    public StatusPingPongPacket() {
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        buffer.readVarIntFromBuffer(); // the packet ID
        this.payload = buffer.readLong();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(new VarInt(0x01));
        buffer.writeLong(payload);
    }
}
