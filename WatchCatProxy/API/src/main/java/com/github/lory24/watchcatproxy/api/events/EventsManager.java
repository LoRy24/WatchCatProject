package com.github.lory24.watchcatproxy.api.events;

import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public abstract class EventsManager {

    /**
     * This function will register an event listener.
     * @param listener The listener
     * @param plugin The plugin that has that listener. Used by the plugin manager to unregister a listener when the
     *               plugin get disabled.
     */
    public abstract void registerEvents(@NotNull final Listener listener, ProxyPlugin plugin);

    /**
     * This function will be used to unregister an event listener.
     * @param listener The listener
     * @param plugin The plugin that has that listener. Same description as up there.
     */
    public abstract void unregisterEvents(@NotNull final Listener listener, ProxyPlugin plugin);

    /**
     * This function will fire an event.
     *
     * @param clazz The class of the event. Used to
     * @param event This param has two functionalities: The first is to define the event that will be fired, and the
     *              second is to give at the listener functions an object to works with.
     * @return if the event is cancelled
     */
    public abstract boolean fireEvent(Class<? extends Event> clazz, Event event) throws InvocationTargetException, IllegalAccessException;
}
