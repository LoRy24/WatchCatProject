package com.github.lory24.watchcatproxy.api.events.defaults;

import com.github.lory24.watchcatproxy.api.events.Event;
import com.github.lory24.watchcatproxy.api.results.HandshakeResult;
import lombok.Getter;

import java.net.InetAddress;

public class HandshakeReceivedEvent extends Event {

    /**
     * The HandshakeResult object
     */
    @Getter
    private final HandshakeResult handshakeResult;

    /**
     * The client's address
     */
    @Getter
    private final InetAddress clientAddress;

    /**
     * Constructor for the HandshakeReceived event. All the passed values will be used by the listeners
     */
    public HandshakeReceivedEvent(HandshakeResult handshakeResult, InetAddress clientAddress) {
        this.handshakeResult = handshakeResult;
        this.clientAddress = clientAddress;
    }
}
