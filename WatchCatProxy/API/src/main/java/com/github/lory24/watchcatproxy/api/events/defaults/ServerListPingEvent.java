package com.github.lory24.watchcatproxy.api.events.defaults;

import com.github.lory24.watchcatproxy.api.events.Event;
import com.github.lory24.watchcatproxy.api.status.ProxyServerStatus;
import lombok.Getter;

import java.net.InetAddress;

public class ServerListPingEvent extends Event {

    /**
     * The client's address
     */
    @Getter
    private final InetAddress clientAddress;

    /**
     * The ProxyServerStatus object. This can be used by the listener to change the values of the Status response
     * packet.
     */
    @Getter
    private final ProxyServerStatus proxyServerStatus;

    /**
     * The constructor for the ServerListPingEvent class. This will initialize the two fields of the class
     * @param clientAddress The address of the client
     * @param proxyServerStatus The proxyServerStatus object. This value will be filled by default values
     */
    public ServerListPingEvent(InetAddress clientAddress, ProxyServerStatus proxyServerStatus) {
        this.clientAddress = clientAddress;
        this.proxyServerStatus = proxyServerStatus;
    }
}
