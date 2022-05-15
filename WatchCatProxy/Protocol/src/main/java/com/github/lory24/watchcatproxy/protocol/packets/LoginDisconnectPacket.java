package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class LoginDisconnectPacket implements Packet {

    @Getter private String jsonChatComponent;

    public LoginDisconnectPacket() {
    }

    public LoginDisconnectPacket(String jsonChatComponent) {
        this.jsonChatComponent = jsonChatComponent;
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        buffer.readVarIntFromBuffer();
        this.jsonChatComponent = buffer.readUTF8String();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException {
        buffer.writeVarIntToBuffer(new VarInt(0x00));
        buffer.writeUTFString(jsonChatComponent);
    }
}
