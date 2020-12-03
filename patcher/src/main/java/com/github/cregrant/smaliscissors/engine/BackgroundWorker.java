package com.github.cregrant.smaliscissors.engine;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BackgroundWorker {
    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static void createIfTerminated() {
        if (BackgroundWorker.executor.isTerminated())
            BackgroundWorker.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
