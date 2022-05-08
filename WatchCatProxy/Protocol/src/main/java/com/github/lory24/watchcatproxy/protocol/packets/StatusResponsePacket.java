package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class StatusResponsePacket implements Packet {
    @Getter
    private String jsonResponse;

    public StatusResponsePacket(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public StatusResponsePacket() {
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        buffer.readVarIntFromBuffer(); // ID
        this.jsonResponse = buffer.readUTF8String();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(new VarInt(0x00));
        buffer.writeUTFString(this.jsonResponse);
    }
}
