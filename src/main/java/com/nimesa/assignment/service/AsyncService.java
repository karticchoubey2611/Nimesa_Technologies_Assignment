package com.nimesa.assignment.service;

import java.util.concurrent.*;

public class AsyncService {

    private final static ExecutorService executor = new ThreadPoolExecutor(20, 50, 10000L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100, true));
    private final static ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(20);

    public static Future<?> submit(Runnable runnable) {
        return executor.submit(runnable);
    }

    public static Future<?> schedule(Runnable runnable, long seconds) {
        return scheduledExecutor.schedule(runnable, seconds, TimeUnit.SECONDS);
    }
}
