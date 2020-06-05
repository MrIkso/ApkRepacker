package com.mrikso.apkrepacker.ui.prererence;

import android.content.res.AssetManager;
import android.util.Log;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.utils.AppUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import brut.androlib.ApkOptions;

public class InitPreference {
    private Preference preference;

    public void init() {
        preference = Preference.getInstance(App.getContext());
        if (!preference.isToolsInstalled()) {
            copyFiles(App.getContext().getAssets(), App.getContext().getFilesDir());
            preference.setToolsInstalled(true);
        }
        loadApkOptions();
    }

    private void loadApkOptions() {
        ApkOptions o = ApkOptions.INSTANCE;
        if (preference.isUseAAPT2()) {
            o.aaptPath = preference.getAapt2Path();
            o.aaptVersion = 2;
            o.useAapt2 = true;
        } else {
            o.aaptPath = preference.getAaptPath();
            o.aaptVersion = 1;
        }
        o.copyOriginalFiles = preference.isCopyOriginalFiles();
        o.debugMode = preference.isDebugModeApk();
        o.verbose = preference.isVerboseModeApk();
        o.frameworkFolderLocation = preference.getFrameworkPath();
    }

    private void copyFiles(AssetManager assets, File outDir) {
        try {
            installAapt(assets, outDir);
            installFramework(assets, outDir);
            installAapt2(assets, outDir);
            loadKey(assets, outDir);
        } catch (IOException ex) {
            Log.e("ERROR", ex.getMessage());
        }
    }

    private void installFramework(AssetManager assets, File outDir) throws IOException {
        InputStream framework_in = assets.open("android.jar");
        File framework = new File(outDir, "framework/1.apk");
        File dir = framework.getParentFile();
        dir.mkdirs();
        OutputStream framework_out = new FileOutputStream(framework);
        IOUtils.copy(framework_in, framework_out);
        framework_in.close();
        framework_out.close();
        preference.setFrameworkPath(dir.getAbsolutePath());
    }

    private void installAapt(AssetManager assets, File outDir) throws IOException {
        File aaptBin = new File(outDir, "aapt");
        InputStream inputStream = assets.open(AppUtils.getArchName() + "/aapt");
        OutputStream outputStream = new FileOutputStream(aaptBin);
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
        aaptBin.setExecutable(true);
        preference.setAaptPath(aaptBin.getAbsolutePath());
    }

    private void installAapt2(AssetManager assets, File outDir) throws IOException {
        File aapt2Bin = new File(outDir, "aapt2");
        InputStream inputStream = assets.open(AppUtils.getArchName() + "/aapt2");
        OutputStream outputStream = new FileOutputStream(aapt2Bin);
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
        aapt2Bin.setExecutable(true);
        preference.setAapt2Path(aapt2Bin.getAbsolutePath());
    }

    private void loadKey(AssetManager assets, File outDir) throws IOException {
        loadPrivateKey(assets, outDir);
        loadCert(assets, outDir);
    }

    private void loadCert(AssetManager assets, File outDir) throws IOException {
        File certPem = new File(outDir, "testkey.x509.pem");
        InputStream cert = assets.open("key/testkey.x509.pem");
        OutputStream outputStream = new FileOutputStream(certPem);
        IOUtils.copy(cert, outputStream);
        outputStream.close();
        cert.close();
        preference.setCertPath(certPem.getAbsolutePath());
    }

    private void loadPrivateKey(AssetManager assets, File outDir) throws IOException {
        File certPk8 = new File(outDir, "testkey.pk8");
        InputStream key = assets.open("key/testkey.pk8");
        OutputStream outputStream = new FileOutputStream(certPk8);
        IOUtils.copy(key, outputStream);
        key.close();
        outputStream.close();
        preference.setPrivateKeyPath(certPk8.getAbsolutePath());
    }
}
