package com.github.lory24.watchcatproxy.proxy.managers;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.plugin.PluginDescription;
import com.github.lory24.watchcatproxy.api.plugin.PluginNotLoadedException;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CatPluginsManager extends PluginsManager {
    private final CatProxyServer catProxyServer;
    // Plugins things
    private final HashMap<String, File> plugins = new HashMap<>();
    private final HashMap<String, ProxyPlugin> pluginsObject = new HashMap<>();
    private final HashMap<String, List<Class<?>>> pluginsClasses = new HashMap<>();

    public CatPluginsManager(CatProxyServer catProxyServer) {
        this.catProxyServer = catProxyServer;
    }

    public void loadAllPlugins(@NotNull File pluginsDirectory)
            throws PluginNotLoadedException, IOException, ClassNotFoundException {
        // Load all the jar files
        File[] files = pluginsDirectory.listFiles();
        if (files == null) return;
        for (File f: files)
            enablePlugin(f);
    }

    public void disableAllPlugins() {

    }

    @Override
    public void enablePlugin(@NotNull File file)
            throws PluginNotLoadedException,
            IOException,
            ClassNotFoundException {
        // Notify loading
        catProxyServer.getLogger().log(LogLevel.INFO, "Enabling " + file.getName());

        // Load jar content in the actual server
        PluginDescription description = loadPluginThings(file);
        if (description == null) throw new PluginNotLoadedException("Error while loading " + file.getName());
        this.plugins.put(description.getName(), file);

        // Call the main class on another thread
        new Thread(() -> {
            getProxyPlugin(description.getName()).onEnable(); // Call onEnable function
        }).start();
    }

    @Override
    public void disablePlugin(String pluginName) {

    }

    @Override
    public ProxyPlugin getProxyPlugin(String pluginName) {
        return this.pluginsObject.get(pluginName);
    }

    // DANGEROUS FUNCTIONS. ONLY EXECUTED FROM CLASS INTERNAL FUNCTIONS

    @SuppressWarnings("resource")
    private PluginDescription loadPluginThings(File file) throws IOException, ClassNotFoundException {
        JarFile jarFile = new JarFile(file.getAbsoluteFile());
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + file.getAbsolutePath() + "!/")});

        // Load the plugin.json file
        URL pluginJSONUrl = urlClassLoader.findResource("plugin.json");
        // TODO LOAD PLUGIN.JSON VALUES AND PUT THEM INTO A NEW PLUGIN DESCRIPTION OBJECT

        // List of all the classes
        List<Class<?>> classes = new ArrayList<>();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry entry = jarFileEntries.nextElement();
            if (entry.isDirectory()) continue;
            if (entry.getName().endsWith(".class")) {
                // Load the class
                String className = entry.getName().substring(0, entry.getName().length() -6);
                className = className.replace('/', '.');
                Class<?> clazz = urlClassLoader.loadClass(className);
                classes.add(clazz);
            }
        }

        // TODO LOAD THE MAIN CLASS

        return null;
    }

    private boolean unloadPluginThings(File file) {
        return false;
    }
}
