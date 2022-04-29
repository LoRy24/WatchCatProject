package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.proxy.managers.CatPluginsManager;
import lombok.SneakyThrows;

public class Launcher {

    @SneakyThrows
    public static void main(String[] args) {
        // Start the server
        WatchCatProxy watchCatProxy = new WatchCatProxy();
        ProxyServer.setInstance(watchCatProxy);
        new Thread(watchCatProxy).start();

        // Add a shutdown task to the proxy server
        Thread shutdownThread = new Thread(() -> {
            // Disable all the plugins
            ((CatPluginsManager) ProxyServer.getInstance().getPluginsManager()).disableAllPlugins();
            // Notify disable
            ProxyServer.getInstance().getLogger().log(LogLevel.SUCCESS, "Bye!");
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
