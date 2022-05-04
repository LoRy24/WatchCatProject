package com.github.lory24.watchcatproxy.protocol;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;

public class VarIntUtil {

    @NotNull
    @Contract("_ -> new")
    public static VarInt readVarInt(@NotNull DataInputStream dataInputStream) throws IOException,
            BufferTypeException {
        int value = 0, length = 0;
        byte current;

        do {
            current = dataInputStream.readByte();
            value |= (current & 127) << length++ * 7;

            if (length > 5) {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((current & 128) == 128);

        return new VarInt(value);
    }
}
