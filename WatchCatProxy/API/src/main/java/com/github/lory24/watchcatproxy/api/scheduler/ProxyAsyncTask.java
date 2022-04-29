package com.github.lory24.watchcatproxy.api.scheduler;

import lombok.Getter;

/**
 * @param taskID The task ID: Used to get the task in the scheduler object. This value is essential in Async tasks
 * @param thread The task thread
 */
public record ProxyAsyncTask(@Getter int taskID, @Getter Thread thread) {
}
