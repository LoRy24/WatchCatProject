package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
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
            ProxyServer.getInstance().getLogger().log(LogLevel.INFO, "Disabling all the plugins, wait...");
            ((CatPluginsManager) ProxyServer.getInstance().getPluginsManager()).disableAllPlugins();
            // Stop the scheduler
            ProxyServer.getInstance().getLogger().log(LogLevel.INFO, "Done. Disabling the proxy's scheduler, wait...");
            ((CatScheduler) ProxyServer.getInstance().getScheduler()).cancelAllTasks();
            // Notify disable
            ProxyServer.getInstance().getLogger().log(LogLevel.SUCCESS, "Done. Bye!");
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
