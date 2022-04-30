package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyAsyncTask;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CatScheduler extends ProxyScheduler {
    private final HashMap<Integer, ProxyAsyncTask> asyncTasks;
    private final HashMap<ProxyAsyncTask, ProxyPlugin> pluginAsyncTasks;

    public CatScheduler() {
        this.asyncTasks = new HashMap<>();
        pluginAsyncTasks = new HashMap<>();
    }

    @Override
    public ProxyAsyncTask runAsync(ProxyPlugin plugin, Runnable runnable) {
        // Generate a task id
        int taskID = generateTaskID();
        // Return the new asyncTask object
        ProxyAsyncTask proxyAsyncTask = new ProxyAsyncTask(taskID, runThread(taskID, runnable));
        this.asyncTasks.put(taskID, proxyAsyncTask);
        if (plugin != null) this.pluginAsyncTasks.put(proxyAsyncTask, plugin);
        return proxyAsyncTask;
    }

    @Override
    public ProxyAsyncTask runAsyncLater(ProxyPlugin plugin, Runnable runnable, int delay) {
        // Generate a task id
        int taskID = generateTaskID();
        // Return the new asyncTask object
        ProxyAsyncTask proxyAsyncTask = new ProxyAsyncTask(taskID, runThread(taskID, () -> {
            try {
                Thread.sleep(50L * delay);
                runnable.run();
            } catch (InterruptedException ignored) {
            }
        }));
        this.asyncTasks.put(taskID, proxyAsyncTask);
        if (plugin != null) this.pluginAsyncTasks.put(proxyAsyncTask, plugin);
        return proxyAsyncTask;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public ProxyAsyncTask runAsyncRepeat(ProxyPlugin plugin, Runnable runnable, int ticks) {
        // Generate a task id
        int taskID = generateTaskID();
        // Return the new asyncTask object
        ProxyAsyncTask proxyAsyncTask = new ProxyAsyncTask(taskID, runThread(taskID, () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(50L * ticks);
                    runnable.run();
                } catch (InterruptedException ignored) {
                }
            }
        }));
        this.asyncTasks.put(taskID, proxyAsyncTask);
        if (plugin != null) this.pluginAsyncTasks.put(proxyAsyncTask, plugin);
        return proxyAsyncTask;
    }

    @Override
    public void cancelTask(int taskID) {
        this.asyncTasks.get(taskID).getThread().interrupt();
        this.asyncTasks.remove(taskID);

        // Remove the task from the plugin's tasks hashmap
        for (ProxyAsyncTask task: this.pluginAsyncTasks.keySet()) {
            if (task.getTaskID() != taskID) continue;
            this.pluginAsyncTasks.remove(task);
            break;
        }
    }

    protected void cancelAllPluginsTask(ProxyPlugin plugin) {
        this.pluginAsyncTasks.forEach((k, v) -> { if (v.equals(plugin)) this.cancelTask(k.getTaskID()); });
    }

    protected void cancelAllTasks() {
        pluginAsyncTasks.clear();
        for (int i: this.asyncTasks.keySet()) this.cancelTask(i);
    }

    // INTERNAL USE FUNCTIONS

    @NotNull
    private Thread runThread(int id, Runnable runnable) {
        Thread taskThread = new Thread(runnable);
        taskThread.setName("CatProxy-AsyncTask#" + id);
        taskThread.start();
        return taskThread;
    }

    // Generate an id
    public int generateTaskID() {
        int id = 0;
        while (asyncTasks.containsKey(id)) id++;
        return id;
    }
}
