package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import lombok.SneakyThrows;

public class Launcher {

    @SneakyThrows
    public static void main(String[] args) {
        // Start the server
        WatchCatProxy watchCatProxy = new WatchCatProxy();
        CatProxyServer.setInstance(watchCatProxy);
        new Thread(watchCatProxy).start();
    }
}
