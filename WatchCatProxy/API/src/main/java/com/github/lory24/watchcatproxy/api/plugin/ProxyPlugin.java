package com.github.lory24.watchcatproxy.api.plugin;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import lombok.Getter;

import java.io.File;

public class ProxyPlugin {

    /**
     * Don't really know why I've put this here... It might be useful later...
     */
    @Getter
    private CatProxyServer proxyServer;

    /**
     * The description of the plugin. This object contains all the plugin.json infos about the plugin, except
     * the main class location.
     */
    @Getter
    private PluginDescription description;

    /**
     * The plugin's file. Private access
     */
    private File pluginFile;

    /**
     * Function called when the server enables the plugin
     */
    public void onEnable() {
    }

    /**
     * Function called when the server disables the plugin.
     */
    public void onDisable() {
    }

    /**
     * This function will log a message in the server console but with the plugin's name as prefix. The prefix is put
     * into square brackets ("[]").
     */
    public void log(LogLevel level, String message) {
        CatProxyServer.getInstance().getLogger().log(level, "[ " + this.description.getName() + " ]" + message);
    }

    /**
     * This function will initialize all the plugin infos. This function will be used by the plugin loader to instance
     * the two fields of this class.
     * @param proxyServer The instanced Proxy server
     * @param description The plugin description object. Created when loaded the jar file
     */
    public void initialize(CatProxyServer proxyServer, PluginDescription description, File pluginFile) {
        this.proxyServer = proxyServer;
        this.description = description;
        this.pluginFile  =  pluginFile;
    }

    /**
     * Get the plugin's data folder. In this folder the plugin can put databases, config files etc.
     * @return The data folder file.
     */
    public File getDataFolder() {
        return new File(pluginFile.getParent(), this.getDescription().getName());
    }
}
