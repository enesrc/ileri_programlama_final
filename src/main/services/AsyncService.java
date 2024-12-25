package main.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncService {
    private final ExecutorService executor;

    public AsyncService(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void executeAsync(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
