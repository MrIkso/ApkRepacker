package com.mrikso.apkrepacker.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.activity.ExceptionActivity;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler previousHandler;

    private static final String TAG = ExceptionHandler.class.getSimpleName();
    private static final boolean DEBUGABLE = false;
    private String versionName;
    private String packageName;
    private String phoneModel;
    private String androidVersion;
    private String board;
    private String brand;
    private String device;
    private String display;
    private String fingerPrint;
    private String host;
    private String id;
    private String manufacturer;
    private String model;
    private String product;
    private String tags;
    private long time;
    private String type;
    private String user;
    private HashMap<String, String> customParameters = new HashMap<>();
    private static ExceptionHandler sInstance;
    private Application application;
    private ExceptionHandler(Application application) {
        this.application = application;
    }

    public static ExceptionHandler get(Application application) {
        if (sInstance == null)
            sInstance = new ExceptionHandler(application);
        return sInstance;
    }

    public void start() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private String createCustomInfoString() {
        StringBuilder customInfo = new StringBuilder();
        for (Object currentKey : customParameters.keySet()) {
            String currentVal = customParameters.get(currentKey);
            customInfo.append(currentKey).append(" = ").append(currentVal).append("\n");
        }
        return customInfo.toString();
    }

    private long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize) / (1024 * 1024);
    }

    private long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return (totalBlocks * blockSize) / (1024 * 1024);
    }

    private void recordInformations(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            //buildNumber = currentVersionNumber(context);
            // Package name
            packageName = pi.packageName;

            // Device model
            phoneModel = Build.MODEL;
            // Android version
            androidVersion = Build.VERSION.RELEASE;

            board = Build.BOARD;
            brand = Build.BRAND;
            device = Build.DEVICE;
            display = Build.DISPLAY;
            fingerPrint = Build.FINGERPRINT;
            host = Build.HOST;
            id = Build.ID;
            model = Build.MODEL;
            product = Build.PRODUCT;
            manufacturer = Build.MANUFACTURER;
            tags = Build.TAGS;
            time = Build.TIME;
            type = Build.TYPE;
            user = Build.USER;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createInformationString() {
        recordInformations(application);
        StringBuilder infoStringBuffer = new StringBuilder();
        infoStringBuffer.append("\nVERSION		: ").append(versionName);
        infoStringBuffer.append("\nPACKAGE      : ").append(packageName);
        //  infoStringBuffer.append("\nFILE-PATH    : ").append(filePath);
        infoStringBuffer.append("\nPHONE-MODEL  : ").append(phoneModel);
        infoStringBuffer.append("\nANDROID_VERSION : ").append(androidVersion);
        infoStringBuffer.append("\nBOARD        : ").append(board);
        infoStringBuffer.append("\nBRAND        : ").append(brand);
        infoStringBuffer.append("\nDEVICE       : ").append(device);
        infoStringBuffer.append("\nTYPE         : ").append(display);
        infoStringBuffer.append("\nFINGER-PRINT : ").append(fingerPrint);
        infoStringBuffer.append("\nHOST         : ").append(host);
        infoStringBuffer.append("\nID           : ").append(id);
        infoStringBuffer.append("\nMODEL        : ").append(model);
        infoStringBuffer.append("\nPRODUCT      : ").append(product);
        infoStringBuffer.append("\nMANUFACTURER : ").append(manufacturer);
        infoStringBuffer.append("\nTAGS         : ").append(tags);
        infoStringBuffer.append("\nTIME         : ").append(time);
        infoStringBuffer.append("\nUSER         : ").append(type);
        infoStringBuffer.append("\nDISPLAY      : ").append(user);
        infoStringBuffer.append("\nTOTAL-INTERNAL-MEMORY     : ").append(getTotalInternalMemorySize() + " mb");
        infoStringBuffer.append("\nAVAILABLE-INTERNAL-MEMORY : ").append(getAvailableInternalMemorySize() + " mb");

        return infoStringBuffer.toString();
    }

    public void uncaughtException(Thread t, Throwable e) {
        showLog("====uncaughtException");

        StringBuilder reportStringBuffer = new StringBuilder();
        reportStringBuffer.append("Error Report collected on : ").append(new Date().toString());
        reportStringBuffer.append("\n\nDevice Informations :\n==============");
        reportStringBuffer.append(createInformationString());
        String customInfo = createCustomInfoString();
        if (!customInfo.equals("")) {
            reportStringBuffer.append("\n\nCustom Informations :\n==============\n");
            reportStringBuffer.append(customInfo);
        }

        reportStringBuffer.append("\n\nStack :\n==============\n");
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        reportStringBuffer.append(result.toString());

        reportStringBuffer.append("\nCause :\n==============");
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            reportStringBuffer.append(result.toString());
            cause = cause.getCause();
        }
        printWriter.close();
        reportStringBuffer.append("\n\n**** End of current Report ***");
        showLog("====uncaughtException \n Report: " + reportStringBuffer.toString());
        //записываем лог в файл
       // writeToFile(reportStringBuffer.toString());

        //вызываем активити с диалогом
        Intent intent = new Intent( App.getContext(), ExceptionActivity.class);
        intent.putExtra("mError", reportStringBuffer.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        App.getContext().startActivity(intent);
      //  Intent intent = new Intent(application, ErrorReporterActivity.class);
      //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      //  application.startActivity(intent);

      //  previousHandler.uncaughtException(t, e);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void showLog(String msg) {
        if (DEBUGABLE) Log.i(TAG, msg);
    }
}

