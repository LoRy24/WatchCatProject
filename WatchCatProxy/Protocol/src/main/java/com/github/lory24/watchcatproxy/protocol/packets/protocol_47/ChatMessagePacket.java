package com.github.lory24.watchcatproxy.protocol.packets.protocol_47;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import com.github.lory24.watchcatproxy.protocol.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ChatMessagePacket implements Packet {
    private String jsonData;
    private byte position;

    public ChatMessagePacket(String jsonData, byte position) {
        this.jsonData = jsonData;
        this.position = position;
    }

    public ChatMessagePacket() {
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        buffer.readVarIntFromBuffer();
        this.jsonData = buffer.readUTF8String();
        this.position = buffer.readByte();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException, IOException {
        buffer.writeVarIntToBuffer(new VarInt(0x02));
        buffer.writeUTFString(this.jsonData);
        buffer.writeByte(this.position);
    }
}
