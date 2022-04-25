package com.github.lory24.watchcatproxy.api.events;

import org.jetbrains.annotations.NotNull;

public abstract class EventsManager {

    /**
     * This function will register an event.
     * @param event The event
     */
    public abstract void registerEvent(@NotNull final Class<? extends Event> event);

    /**
     * This function will be used to unregister an event.
     * @param event The event
     */
    public abstract void unregisterEvent(@NotNull final Class<? extends Event> event);

    /**
     * This function will register an event listener.
     * @param listener The listener
     */
    public abstract void registerEvents(@NotNull final Listener listener);

    /**
     * This function will be used to unregister an event listener.
     * @param listener The listener
     */
    public abstract void unregisterEvents(@NotNull final Listener listener);

    /**
     * This function will fire an event.
     * @param event This param has two functionalities: The first is to define the event that will be fired, and the
     *              second is to give at the listener functions an object to works with.
     */
    public abstract void fireEvent(final Event event);
}
