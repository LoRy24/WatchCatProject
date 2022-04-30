package com.github.lory24.watchcatproxy.proxy.timeout;

import com.github.lory24.watchcatproxy.api.scheduler.ProxyAsyncTask;
import com.github.lory24.watchcatproxy.proxy.WatchCatProxy;
import lombok.Getter;

import java.net.InetAddress;

public class TimeoutAccount {

    // The client address and the ProxyServer object
    @Getter
    private final InetAddress address;
    private final WatchCatProxy watchCatProxy;

    // "Public" fields
    @Getter private TimeoutLevel timeoutLevel;
    @Getter private boolean inTimeout;

    // Internal use fields
    private ProxyAsyncTask asyncTask;

    public TimeoutAccount(InetAddress address, WatchCatProxy watchCatProxy) {
        this.address = address;
        this.watchCatProxy = watchCatProxy;
    }

    public void setTimeout(TimeoutLevel level) {
        this.timeoutLevel = level;

        // Check if there is already a running task
        if (this.inTimeout) {
            // Cancel the precedent task
            watchCatProxy.getScheduler().cancelTask(asyncTask.taskID());
        }

        // Run the new task
        this.inTimeout = true;
        this.asyncTask = watchCatProxy.getScheduler().runAsyncLater(null, () -> this.inTimeout = false, this.timeoutLevel.getTicks());
    }
}
