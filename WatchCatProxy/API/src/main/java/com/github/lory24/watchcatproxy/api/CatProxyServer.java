package com.github.lory24.watchcatproxy.api;

import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public abstract class CatProxyServer {

    /**
     * The server instance. Used by the API to get the instanced proxy object. This value is set when the Proxy has
     * been started.
     */
    @Getter @Setter
    private static CatProxyServer instance;

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
    public abstract Logger getLogger();
}
