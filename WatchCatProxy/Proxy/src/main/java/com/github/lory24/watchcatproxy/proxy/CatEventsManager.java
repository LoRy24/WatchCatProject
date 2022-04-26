package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.events.Event;
import com.github.lory24.watchcatproxy.api.events.EventListener;
import com.github.lory24.watchcatproxy.api.events.EventsManager;
import com.github.lory24.watchcatproxy.api.events.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CatEventsManager extends EventsManager {

    private final List<Listener> listeners = new ArrayList<>();

    @Override
    public void registerEvents(@NotNull Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregisterEvents(@NotNull Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public boolean fireEvent(@NotNull Class<? extends Event> clazz, Event event) throws InvocationTargetException, IllegalAccessException {

        // Select the methods
        final Map<Integer, Method> functions = new HashMap<>();
        final HashMap<Method, Listener> listenersHashMap = new HashMap<>();

        // Add all the methods
        for (Listener listener: listeners) {
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
}
