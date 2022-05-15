package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.ProxyServer;
import com.github.lory24.watchcatproxy.api.events.Event;
import com.github.lory24.watchcatproxy.api.events.EventListener;
import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.events.Listener;
import com.github.lory24.watchcatproxy.api.logging.LogLevel;
import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CatEventsManager extends EventsManager {

    private final HashMap<Listener, ProxyPlugin> listeners = new HashMap<>();

    public CatEventsManager() {
        ProxyServer.getInstance().getLogger().log(LogLevel.INFO, "Events manager has been loaded! Loading plugins, wait.");
    }

    @Override
    public void registerEvents(@NotNull Listener listener, ProxyPlugin plugin) {
        this.listeners.put(listener, plugin);
    }

    @Override
    public void unregisterEvents(@NotNull Listener listener, ProxyPlugin plugin) {
        this.listeners.remove(listener);
    }

    @Override
    public boolean fireEvent(@NotNull Class<? extends Event> clazz, Event event) throws InvocationTargetException, IllegalAccessException {

        // Select the methods
        final Map<Integer, Method> functions = new HashMap<>();
        final HashMap<Method, Listener> listenersHashMap = new HashMap<>();

        // Add all the methods
        for (Listener listener: listeners.keySet()) {
            for (Method method: listener.getClass().getMethods()) {
                if (method.isAnnotationPresent(EventListener.class)) {
                    if (method.getParameters().length < 1 || !method.getParameters()[0].getType().equals(clazz)) continue;
                    EventListener annotation = method.getAnnotation(EventListener.class);
                    functions.put(annotation.priority().getLevel(), method);
                    listenersHashMap.put(method, listener);
                }
            }
        }

        // Create a tree map
        final TreeMap<Integer, Method> sortedMethods = new TreeMap<>(functions);

        // Run all the methods
        boolean cancelled = false;
        for (Method m: sortedMethods.values()) {
            if (cancelled && !m.getAnnotation(EventListener.class).ignoreCancelled()) continue;
            m.invoke(listenersHashMap.get(m), event);
            cancelled = event.isCancelled();
        }

        // Return the cancelled result
        return cancelled;
    }

    protected void unregisterAllPluginListener(ProxyPlugin plugin) {
        for(Map.Entry<Listener, ProxyPlugin> entry: this.listeners.entrySet()) {
            if (!entry.getValue().equals(plugin)) return;
            listeners.remove(entry.getKey(), entry.getValue());
        }
    }
}
