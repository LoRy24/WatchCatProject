package com.github.lory24.watchcatproxy.protocol;

public class VarInt extends Number {
    public final PacketBuffer varIntBuffer;

    public VarInt(int number) throws BufferTypeException {
        varIntBuffer = new PacketBuffer();
        while ((number & -128) != 0) {
            varIntBuffer.writeByte((byte) (number & 127 | 128));
            number >>>= 7;
        }
        varIntBuffer.writeByte((byte) number);
    }

    @Override
    public int intValue() {
        byte[] copiedBytes = this.varIntBuffer.getBufferBytes();
        int index = 0;

        int value = 0, length = 0;
        byte current;

        do {
            current = copiedBytes[index++];
            value |= (current & 127) << length++ * 7;

            if (length > 5) {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((current & 128) == 128);

        return value;
    }

    @Override
    public long longValue() {
        return Long.parseLong(String.valueOf(intValue()));
    }

    @Override
    public float floatValue() {
        return Float.parseFloat(String.valueOf(intValue()));
    }

    @Override
    public double doubleValue() {
        return Double.parseDouble(String.valueOf(intValue()));
    }
}
