package com.github.cregrant.smaliscissors.engine;

public interface DexExecutor {
    void runDex(String dexPath, String entrance, String mainClass, String apkPath, String zipPath, String projectPath, String param, String tempDir);
}
