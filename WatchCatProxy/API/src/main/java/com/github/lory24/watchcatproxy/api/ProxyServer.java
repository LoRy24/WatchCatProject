package com.github.lory24.watchcatproxy.api;

import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.logging.ProxyLogger;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyScheduler;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public abstract class ProxyServer {

    /**
     * The server instance. Used by the API to get the instanced proxy object. This value is set when the Proxy has
     * been started.
     */
    @Getter @Setter
    private static ProxyServer instance;

    /**
     * Get the events manager of the server. It will return the Server's instanced events manager.
     *
     * @return The events manager
     */
    public abstract EventsManager getEventsManager();

    /**
     * Get the version of the server. This is an abstract function, and it's implemented by the server instance
     *
     * @return Server version
     */
    public abstract String getVersion();

    /**
     * Get the logger. This is an abstract function that will be implemented by the proxy instance. It will return
     * the instanced server logger.
     *
     * @return The logger.
     */
    public abstract ProxyLogger getLogger();

    /**
     * Get the plugins' manager. It will manage all the plugins.
     *
     * @return The manager object
     */
    public abstract PluginsManager getPluginsManager();

    /**
     * Get the server's scheduler object. It will be used to run tasks in an "optimized" way. It will be also used
     * to run asynchronously tasks.
     *
     * @return The instanced scheduler object
     */
    public abstract ProxyScheduler getScheduler();

    /**
     * This function will return the online proxiedPlayers hashmap. The hashmap has as key the name of the player, and
     * as object the ProxiedPlayer instance.
     *
     * @return The proxiedPlayers list
     */
    public abstract HashMap<String, ProxiedPlayer> getProxiedPlayers();
}
