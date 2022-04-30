package com.github.lory24.watchcatproxy.proxy;

import com.github.lory24.watchcatproxy.api.plugin.ProxyPlugin;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyAsyncTask;
import com.github.lory24.watchcatproxy.api.scheduler.ProxyScheduler;

import java.util.HashMap;

public class CatScheduler extends ProxyScheduler {
    private final HashMap<Integer, ProxyAsyncTask> asyncTasks;

    public CatScheduler() {
        this.asyncTasks = new HashMap<>();
    }

    @Override
    public ProxyAsyncTask runAsync(ProxyPlugin plugin, Runnable runnable) {
        // Generate a task id
        int taskID = generateTaskID();
        Thread taskThread = new Thread(runnable);
        taskThread.setName("CatProxy-AsyncTask#" + taskID);
        taskThread.start();
        // Return the new asyncTask object
        ProxyAsyncTask proxyAsyncTask = new ProxyAsyncTask(taskID, taskThread);
        this.asyncTasks.put(taskID, proxyAsyncTask);
        return proxyAsyncTask;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public ProxyAsyncTask runAsyncRepeat(ProxyPlugin plugin, Runnable runnable, int ticks) {
        // Generate a task id
        int taskID = generateTaskID();
        // Create the thread
        Thread taskThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(50L * ticks);
                    runnable.run();
                } catch (InterruptedException ignored) {
                }
            }
        });
        taskThread.setName("CatProxy-AsyncTask#" + taskID);
        taskThread.start();

        // Return the new asyncTask object
        ProxyAsyncTask proxyAsyncTask = new ProxyAsyncTask(taskID, taskThread);
        this.asyncTasks.put(taskID, proxyAsyncTask);
        return proxyAsyncTask;
    }

    @Override
    public void cancelTask(int taskID) {
        this.asyncTasks.get(taskID).getThread().interrupt();
        this.asyncTasks.remove(taskID);
    }

    // Generate an id
    public int generateTaskID() {
        int id = 0;
        while (asyncTasks.containsKey(id)) id++;
        return id;
    }
}
