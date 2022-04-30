package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.plugin.PluginDescription;
import com.github.lory24.watchcatproxy.api.plugin.PluginNotLoadedException;
import com.github.lory24.watchcatproxy.api.plugin.PluginsManager;
import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import com.github.lory24.watchcatproxy.proxy.CatEventsManager;
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

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class CatPluginsManager extends PluginsManager {

    // Private server object
    private final ProxyServer catProxyServer;

    // Plugins things
    private final HashMap<String, File> plugins = new HashMap<>();
    private final HashMap<String, ProxyPlugin> pluginsObject = new HashMap<>();
    private final HashMap<String, List<Class<?>>> pluginsClasses = new HashMap<>();
    private final HashMap<String, Thread> pluginsThreads = new HashMap<>();

    public CatPluginsManager(ProxyServer catProxyServer) {
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
            throws IOException, ClassNotFoundException {
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
        for (String pluginName: this.plugins.keySet()) {
            catProxyServer.getLogger().log(LogLevel.INFO, "Disabling " + pluginName + "!");
            disablePlugin(pluginName);
        }
    }

    @Override
    public void enablePlugin(@NotNull File file) throws PluginNotLoadedException, IOException, ClassNotFoundException {
        // Notify loading
        catProxyServer.getLogger().log(LogLevel.INFO, "Enabling " + file.getName());

        // Load jar content in the actual server
        PluginDescription description = loadPluginThings(file);
        this.plugins.put(description.getName(), file);

        // Call the main class on another thread
        Thread pluginThread = new Thread(() -> {
            getProxyPlugin(description.getName()).onEnable(); // Call onEnable function
        });

        pluginThread.start();

        // Add the plugin's thread to a hashmap
        this.pluginsThreads.put(description.getName(), pluginThread);
    }

    @Override
    public void disablePlugin(String pluginName) {
        // Call onDisable function
        new Thread(() -> getProxyPlugin(pluginName).onDisable()).start();

        // Unregister the listeners
        ((CatEventsManager) catProxyServer.getEventsManager()).unregisterAllPluginListener(pluginsObject.get(pluginName));

        // Unload all the plugin's things
        unloadPluginThings(pluginName, plugins.get(pluginName));
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

    @SuppressWarnings("resource")
    @NotNull
    private PluginDescription loadPluginThings(@NotNull File file)
            throws IOException, ClassNotFoundException,
            PluginNotLoadedException {

        JarFile jarFile = new JarFile(file.getAbsoluteFile());
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + file.getAbsolutePath() + "!/")});

        // Load the plugin.json file
        JSONObject pluginJSON = this.getJsonObjectFromPluginJSON(urlClassLoader, file);
        PluginDescription pluginDescription = new PluginDescription(pluginJSON.getString("name"), pluginJSON.getString("version"), pluginJSON.getString("author"));

        // List of all the classes
        List<Class<?>> classes = new ArrayList<>();
        String mainClassPath = pluginJSON.getString("mainClass");
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

    private void unloadPluginThings(String pluginName, File file) {
        // Interrupt the plugin thread
        this.pluginsThreads.get(pluginName).interrupt();

        // Remove all the hashmaps
        this.pluginsThreads.remove(pluginName);
        this.plugins.remove(pluginName); this.pluginsObject.remove(pluginName);

        // TODO Unload the classes
        this.pluginsClasses.remove(pluginName);
    }

    // PRIVATE UTILS

    @NotNull
    private JSONObject getJsonObjectFromPluginJSON(@NotNull URLClassLoader urlClassLoader,
                                                   File file)
            throws IOException, PluginNotLoadedException {
        InputStream pluginJSONInputStream = urlClassLoader.getResourceAsStream("plugin.json");
        if (pluginJSONInputStream == null) throw new PluginNotLoadedException("Error while loading " + file.getName() + ". Is that a plugin?");

        JSONObject pluginJSON = new JSONObject(new String(pluginJSONInputStream.readAllBytes()));
        pluginJSONInputStream.close();
        return pluginJSON;
    }

    @Contract("_ -> fail")
    private void fireClassLoadingErrorException(@NotNull File file)
            throws PluginNotLoadedException {
        throw new PluginNotLoadedException("Error while loading the main class of " + file.getName() + ".");
    }
}
