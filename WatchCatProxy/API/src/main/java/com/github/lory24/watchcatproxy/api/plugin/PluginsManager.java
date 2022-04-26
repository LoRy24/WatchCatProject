package com.github.lory24.watchcatproxy.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class PluginsManager {

    /**
     * Function used to load a plugin by the file. This function will insert the plugin into the server plugins
     * register and call the {@code onEnable()} function of the main class. The plugin will be run in another thread,
     * in order to avoid server crash.
     * @param file The plugin file located into the server plugins folder
     * @throws PluginNotLoadedException When an error is occurred during plugin enabling sequence, this exception
     * will be thrown
     * @throws IOException When there is a problem with the jar file loading (Internal error)
     * @throws ClassNotFoundException Another internal error exception
     */
    public abstract void enablePlugin(@NotNull File file) throws PluginNotLoadedException, IOException, ClassNotFoundException;

    /**
     * Unload a plugin by his name. The plugin will be unloaded with all his classes and resources from the server.
     * @param pluginName The name of the plugin that will be unloaded
     */
    public abstract void disablePlugin(String pluginName);

    /**
     * Get the plugin instanced class by the plugin's name. The value is get from an HashMap.
     * @param pluginName The name of the plugin
     */
    public abstract ProxyPlugin getProxyPlugin(String pluginName);
}
