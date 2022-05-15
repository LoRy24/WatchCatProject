package com.github.lory24.watchcatproxy.proxy.connection;

import com.github.lory24.watchcatproxy.proxy.InitializationState;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

public class SubServerInitialHandler {

    @Getter @Setter
    private InitializationState state;

    @Getter
    private Socket serverConn;

    // Player infos
    private final CatProxiedPlayer catProxiedPlayer;

    public SubServerInitialHandler(CatProxiedPlayer catProxiedPlayer) {
        this.catProxiedPlayer = catProxiedPlayer;
    }


    public void initializeConnectionToSubServer(SubServerInfo subServerInfo) {
        processHandshake();
    }

    public void processHandshake() {

    }
}
