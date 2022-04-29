package com.github.lory24.watchcatproxy.api.scheduler;

import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;

public abstract class CatProxyScheduler {

    /**
     * Run an async task.
     * @param plugin The plugin object. Used by the plugin loader to close all the tasks when a plugin get disabled
     * @param runnable The task content
     * @return The ProxyAsync task
     */
    public abstract ProxyAsyncTask runAsync(ProxyPlugin plugin, Runnable runnable);

    /**
     * Run a repeating async task
     * @param plugin The plugin object. Same reason as up there
     * @param runnable The task content
     * @param ticks Ticks between every run.
     * @return The ProxyAsync task
     */
    public abstract ProxyAsyncTask runAsyncRepeat(ProxyPlugin plugin, Runnable runnable, int ticks);

    /**
     * Cancel a task by its id. Used to cancel async tasks
     * @param taskID The task's id
     */
    public abstract void cancelTask(int taskID);
}
