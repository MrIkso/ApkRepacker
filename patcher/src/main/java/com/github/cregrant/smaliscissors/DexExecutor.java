package com.github.cregrant.smaliscissors;

public interface DexExecutor {
    void runDex(String dexPath, String entrance, String mainClass, String apkPath, String zipPath, String projectPath, String param, String tempDir);
}
