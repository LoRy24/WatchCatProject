package com.github.lory24.watchcatproxy.protocol.packets;

import com.github.lory24.watchcatproxy.protocol.BufferTypeException;
import com.github.lory24.watchcatproxy.protocol.PacketBuffer;
import com.github.lory24.watchcatproxy.protocol.ReadExploitException;

public interface Packet {

    void readData(PacketBuffer buffer) throws BufferTypeException, ReadExploitException;

    void writeData(PacketBuffer buffer) throws BufferTypeException;
}
