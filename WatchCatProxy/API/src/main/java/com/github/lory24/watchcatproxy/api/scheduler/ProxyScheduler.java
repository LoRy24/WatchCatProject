package com.github.lory24.watchcatproxy.api.scheduler;

import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;

public abstract class ProxyScheduler {

    /**
     * Run an async task.
     * @param plugin The plugin object. Used by the plugin loader to close all the tasks when a plugin get disabled
     * @param runnable The task content
     * @return The ProxyAsync task
     */
    public abstract ProxyAsyncTask runAsync(ProxyPlugin plugin, Runnable runnable);

    /**
     * Run an async task with a delay.
     * @param plugin The plugin object. Same reason as up there
     * @param runnable The task content
     * @param delay How many ticks to wait before run the task
     * @return The ProxyAsync task
     */
    public abstract ProxyAsyncTask runAsyncLater(ProxyPlugin plugin, Runnable runnable, int delay);

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
