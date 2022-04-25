package com.github.lory24.watchcatproxy.protocol;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class VarIntUtils {

    public static int readVarInt(@NotNull DataInputStream dataInputStream) throws IOException {
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

        return value;
    }

    public void writeVarInt(@NotNull DataOutputStream dataOutputStream, int input) throws IOException {
        while ((input & -128) != 0) {
            dataOutputStream.writeByte(input & 127 | 128);
            input >>>= 7;
        }
        dataOutputStream.writeByte(input);
    }
}
