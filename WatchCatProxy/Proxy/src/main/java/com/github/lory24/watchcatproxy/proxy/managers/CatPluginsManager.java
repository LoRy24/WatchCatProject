package com.github.lory24.watchcatproxy.proxy.managers;

import com.github.lory24.watchcatproxy.api.CatProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.plugin.PluginDescription;
import com.github.lory24.watchcatproxy.api.plugin.PluginNotLoadedException;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setup()
            throws IOException, URISyntaxException, ClassNotFoundException {
        // Create the plugin folder if it doesn't exists
        if (!this.getPluginsFolder().exists()) this.getPluginsFolder().mkdir();
        // Load all the plugins
        loadAllPlugins(this.getPluginsFolder());
    }

    public void loadAllPlugins(@NotNull File pluginsDirectory)
            throws IOException, ClassNotFoundException, URISyntaxException {
        // Load all the jar files
        File[] files = pluginsDirectory.listFiles();
        if (files == null) return;
        for (File f: files) {
            try {
                enablePlugin(f);
            } catch (PluginNotLoadedException e) {
                this.catProxyServer.getLogger().log(LogLevel.WARNING, e.getMessage());
            }
        }
    }

    public void disableAllPlugins() {

    }

    @Override
    public void enablePlugin(@NotNull File file) throws PluginNotLoadedException, IOException, ClassNotFoundException, URISyntaxException {
        // Notify loading
        catProxyServer.getLogger().log(LogLevel.INFO, "Enabling " + file.getName());

        // Load jar content in the actual server
        PluginDescription description = loadPluginThings(file);
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

    @Override
    public File getPluginsFolder() {
        return new File("./plugins/");
    }

    // DANGEROUS FUNCTIONS. ONLY EXECUTED FROM CLASS INTERNAL FUNCTIONS

    // TODO OPTIMIZE THIS
    @SuppressWarnings("resource")
    @NotNull
    private PluginDescription loadPluginThings(@NotNull File file)
            throws IOException, ClassNotFoundException, URISyntaxException,
            PluginNotLoadedException {

        JarFile jarFile = new JarFile(file.getAbsoluteFile());
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + file.getAbsolutePath() + "!/")});

        // Load the plugin.json file
        PluginDescription pluginDescription = this.getDescriptionFromPluginJSON(urlClassLoader, file);

        // List of all the classes
        List<Class<?>> classes = new ArrayList<>();
        String mainClassPath = pluginJSON.getString("mainClass"); // TODO FIX THIS
        ProxyPlugin plugin = null;

        Enumeration<JarEntry> jarFileEntries = jarFile.entries();
        while (jarFileEntries.hasMoreElements()) {
            JarEntry entry = jarFileEntries.nextElement();
            if (entry.isDirectory()) continue;
            if (entry.getName().endsWith(".class")) {
                // Load the class
                String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                Class<?> clazz = urlClassLoader.loadClass(className);
                classes.add(clazz);

                if (!className.equals(mainClassPath)) continue;
                try {
                    plugin = (ProxyPlugin) clazz.getConstructor().newInstance();
                    plugin.initialize(this.catProxyServer, pluginDescription, file);
                } catch (Exception e) {
                    fireClassLoadingErrorException(file);
                }
            }
        }

        if (plugin == null) fireClassLoadingErrorException(file);

        // Put all the values and return
        String name = pluginDescription.getName();
        this.pluginsClasses.put(name, classes);
        this.pluginsObject.put(name, plugin);
        return pluginDescription;
    }

    private boolean unloadPluginThings(File file) {
        return false;
    }

    // PRIVATE UTILS

    private PluginDescription getDescriptionFromPluginJSON(URLClassLoader urlClassLoader, File file) throws IOException, PluginNotLoadedException {
        InputStream pluginJSONInputStream = urlClassLoader.getResourceAsStream("plugin.json");
        if (pluginJSONInputStream == null) throw new PluginNotLoadedException("Error while loading " + file.getName() + ". Is that a plugin?");

        JSONObject pluginJSON = new JSONObject(new String(pluginJSONInputStream.readAllBytes()));
        pluginJSONInputStream.close();
        PluginDescription pluginDescription = new PluginDescription(pluginJSON.getString("name"), pluginJSON.getString("version"), pluginJSON.getString("author"));
    }

    @Contract("_ -> fail")
    private void fireClassLoadingErrorException(@NotNull File file)
            throws PluginNotLoadedException {
        throw new PluginNotLoadedException("Error while loading the main class of " + file.getName() + ".");
    }
}
