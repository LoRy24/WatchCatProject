package com.github.lory24.watchcatproxy.protocol;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;

public class VarIntUtil {

    @NotNull
    @Contract("_ -> new")
    public static VarInt readVarInt(@NotNull DataInputStream dataInputStream) throws IOException,
            BufferTypeException {
        read: {
            int value = 0, length = 0;
            byte current;

            do {
                current = (byte) dataInputStream.read();
                if (current == -1) break read;
                value |= (current & 127) << length++ * 7;

                if (length > 5) {
                    throw new RuntimeException("VarInt too big");
                }
            }
            while ((current & 128) == 128);

            return new VarInt(value);
        }
        return new VarInt(-1);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static VarInt readEncryptedVarInt(@NotNull DataInputStream dataInputStream, @NotNull EncryptionUtil encryptionUtil) throws IOException,
            BufferTypeException {
        read: {
            int value = 0, length = 0;
            byte current;

            do {
                int read = dataInputStream.read();
                if (read == -1) break read;
                current = encryptionUtil.decryptByte((byte) read);
                value |= (current & 127) << length++ * 7;

                if (length > 5) {
                    throw new RuntimeException("VarInt too big");
                }
            }
            while ((current & 128) == 128);

            return new VarInt(value);
        }
        return new VarInt(-1);
    }
}
