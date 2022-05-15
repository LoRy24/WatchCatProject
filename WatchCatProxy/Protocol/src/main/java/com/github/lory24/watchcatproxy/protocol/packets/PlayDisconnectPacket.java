package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;
import com.github.lory24.watchcatproxy.protocol.VarInt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PlayDisconnectPacket implements Packet {

    @Getter private int ID;
    @Getter private String chatComponentJSON;

    public PlayDisconnectPacket(int ID, String chatComponentJSON) {
        this.ID = ID;
        this.chatComponentJSON = chatComponentJSON;
    }

    public PlayDisconnectPacket() {
    }

    @Override
    public void readData(@NotNull PacketBuffer buffer) throws BufferTypeException, ReadExploitException {
        this.ID = buffer.readVarIntFromBuffer().intValue();
        this.chatComponentJSON = buffer.readUTF8String();
    }

    @Override
    public void writeData(@NotNull PacketBuffer buffer) throws BufferTypeException, IOException {
        buffer.writeVarIntToBuffer(new VarInt(this.ID));
        buffer.writeUTFString(this.chatComponentJSON);
    }
}
