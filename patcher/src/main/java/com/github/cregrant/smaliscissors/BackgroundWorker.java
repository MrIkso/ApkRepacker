package com.github.cregrant.smaliscissors;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackgroundWorker {
    public static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void createIfTerminated() {
        if (BackgroundWorker.executor.isTerminated())
            BackgroundWorker.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static void computeAndDestroy() {
        try {
            BackgroundWorker.executor.shutdown();
            BackgroundWorker.executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
