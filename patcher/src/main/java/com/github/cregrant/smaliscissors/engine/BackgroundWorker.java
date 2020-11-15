package com.github.cregrant.smaliscissors.engine;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BackgroundWorker {
    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}
