package com.github.lory24.watchcatproxy.protocol;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class PacketBuffer {

    private ArrayList<Byte> bytes;
    private byte[] finalBytes;
    private final BufferType bufferType;
    protected int pointer;

    public PacketBuffer() {
        this.bytes = new ArrayList<>();
        this.bufferType = BufferType.WRITE;
    }

    public PacketBuffer(byte[] defaultBytes) {
        this.finalBytes = defaultBytes;
        this.bufferType = BufferType.READ;
    }

    // SPECIAL GETTER

    public byte[] getBufferBytes() {
        if (this.bufferType == BufferType.WRITE) {
            this.finalBytes = new byte[this.bytes.size()];
            for (int i = 0; i < this.bytes.size(); i++) {
                this.finalBytes[i] = bytes.get(i);
            }
        }
        return finalBytes;
    }

    // BYTES

    public byte readByte()
            throws ReadExploitException {
        try {
            if (this.bufferType == BufferType.WRITE) {
                throw new BufferTypeException(this.bufferType);
            }
            return this.finalBytes[pointer++];
        } catch (Exception e) {
            throw new ReadExploitException(e.getMessage());
        }
    }

    public void writeByte(byte b)
            throws BufferTypeException {
        if (this.bufferType == BufferType.READ) {
            throw new BufferTypeException(this.bufferType);
        }
        this.bytes.add(b);
    }

    public byte[] readBytes(int amount) throws ReadExploitException {
        byte[] resultBytes = new byte[amount];
        for (int i = 0; i < amount; i++) {
            resultBytes[i] = readByte();
        }
        return resultBytes;
    }

    public void writeBytes(byte[] bytes, int offset, int length) throws BufferTypeException {
        for (int i = 0; i < length; i++) {
            writeByte(bytes[offset + i]);
        }
    }

    public void writeBytes(@NotNull byte[] bytes) throws BufferTypeException {
        for (byte b: bytes) writeByte(b);
    }

    // INTEGER

    public int readInt() throws ReadExploitException {
        int ch1 = this.readByte(), ch2 = this.readByte(), ch3 = this.readByte(), ch4 = this.readByte();
        return (((ch1 & 0xFF) << 24) + ((ch2 & 0xFF) << 16) + ((ch3 & 0xFF) << 8) + (ch4 & 0xFF));
    }

    public void writeInt(int i) throws BufferTypeException {
        this.writeByte((byte)(i >> 24));
        this.writeByte((byte)(i >> 16));
        this.writeByte((byte)(i >> 8));
        this.writeByte((byte)(i));
    }

    public VarInt readVarIntFromBuffer() throws BufferTypeException, ReadExploitException {
        int value = 0, length = 0;
        byte current;

        do {
            current = this.readByte();
            value |= (current & 127) << length++ * 7;

            if (length > 5) {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((current & 128) == 128);
        return new VarInt(value);
    }


    public void writeVarIntToBuffer(VarInt input) throws BufferTypeException {
        this.writeBytes(input.varIntBuffer.getBufferBytes());
    }

    // LONG

    public long readVarLong() throws ReadExploitException {
        long value = 0L, length = 0;
        byte current;

        do {
            current = readByte();
            value |= (long)(current & 127) << length++ * 7;

            if (length > 10) {
                throw new RuntimeException("VarLong too big");
            }
        }
        while ((current & 128) == 128);

        return value;
    }

    public void writeVarLong(long value) throws BufferTypeException {
        while ((value & -128L) != 0L) {
            this.writeByte((byte) ((int)(value & 127L) | 128));
            value >>>= 7;
        }
        this.writeByte((byte) value);
    }

    // UTF-8 STRING

    public String readUTF8String() throws ReadExploitException, BufferTypeException {
        int length = this.readVarIntFromBuffer().toInteger();
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    public void writeUTFString(@NotNull String s) throws BufferTypeException {
        this.writeVarIntToBuffer(new VarInt(s.length()));
        this.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    // Short

    public void writeUnsignedShort(int i) throws BufferTypeException {
        this.writeByte((byte) ((i >> 8) & 0xFF));
        this.writeByte((byte) ((i) & 0xFF));
    }

    public int readUnsignedShort() throws ArrayIndexOutOfBoundsException, ReadExploitException {
        byte[] bytes = this.readBytes(2);
        return (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }

    // Long

    public void writeLong(long l) throws BufferTypeException {
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            writeByte((byte)(l & 0xFF));
            l >>= Byte.SIZE;
        }
    }

    public long readLong() throws ReadExploitException {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (readByte() & 0xFF);
        }
        return result;
    }
}
