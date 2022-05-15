package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoginSuccessPacket implements Packet {

    private String username;
    private UUID uuid;

    public LoginSuccessPacket(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public LoginSuccessPacket() {
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        buffer.readVarIntFromBuffer();
        this.uuid = UUID.nameUUIDFromBytes(buffer.readUTF8String().getBytes(StandardCharsets.UTF_8));
        this.username = buffer.readUTF8String();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(new VarInt(0x02));
        buffer.writeUTFString(this.uuid.toString());
        buffer.writeUTFString(username);
    }
}
