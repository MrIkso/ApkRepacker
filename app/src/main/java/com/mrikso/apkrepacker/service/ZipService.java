/*
 * Copyright (C) 2018 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrikso.apkrepacker.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;


import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.CompressOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.ExtractOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.FileOperationRunnerInjector;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CompressArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.ExtractArguments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZipService extends IntentService {
    private static final String ACTION_COMPRESS = "com.mrikso.apkrepacker.action.COMPRESS";
    private static final String ACTION_EXTRACT = "com.mrikso.apkrepacker.action.EXTRACT";
    private static final String EXTRA_FILES = "com.mrikso.apkrepacker.action.FILES";

    public ZipService() {
        super(ZipService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<FileHolder> files = intent.getParcelableArrayListExtra(EXTRA_FILES);
        File to = new File(intent.getData().getPath());

        if (ACTION_COMPRESS.equals(intent.getAction())) {
            try {
                FileOperationRunnerInjector.operationRunner(this).run(new CompressOperation(this), CompressArguments.compressArgs(to, files));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (ACTION_EXTRACT.equals(intent.getAction())) {
            try {
                FileOperationRunnerInjector.operationRunner(this).run(new ExtractOperation(this), ExtractArguments.extractArgs(to, files));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void extractTo(@NonNull Context c, final FileHolder tbe, File extractTo) {
        extractTo(c, Collections.singletonList(tbe), extractTo);
    }

    public static void compressTo(@NonNull Context c, final FileHolder tbc, File compressTo) {
        compressTo(c, Collections.singletonList(tbc), compressTo);
    }

    public static void extractTo(@NonNull Context c, List<FileHolder> tbe, File extractTo) {
        Intent i = new Intent(ACTION_EXTRACT);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(extractTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbe instanceof ArrayList
                ? (ArrayList<FileHolder>) tbe
                : new ArrayList<>(tbe));
        c.startService(i);
    }

    public static void compressTo(@NonNull Context c, List<FileHolder> tbc, File compressTo) {
        Intent i = new Intent(ACTION_COMPRESS);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(compressTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbc instanceof ArrayList
                ? (ArrayList<FileHolder>) tbc
                : new ArrayList<>(tbc));
        c.startService(i);
    }
}
