/*
 * Copyright (C) 2018 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrikso.apkrepacker.ui.filemanager.storage.operation;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.storage.DocumentFileUtils;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.RenameArguments;
import com.mrikso.apkrepacker.ui.filemanager.utils.MediaScannerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class RenameOperation extends FileOperation<RenameArguments> {
    private final Context context;
    private List<String> affectedPaths = new ArrayList<>();

    public RenameOperation(Context context) {
        this.context = context;
    }

    @Override
    public boolean operate(RenameArguments args) {
        File from = args.getFileToRename();
        File dest = args.getTarget();

        return dest.exists() || from.renameTo(dest);
    }

    @Override
    public boolean operateSaf(RenameArguments args) {
        File from = args.getFileToRename();
        File dest = args.getTarget();

        if (dest.exists()) {
            return true;
        } else {
            DocumentFile safFrom = DocumentFileUtils.findFile(context, from);
            return safFrom != null && safFrom.renameTo(args.getTarget().getName());
        }
    }

    @Override
    protected void onStartOperation(RenameArguments args) {
        File from = args.getFileToRename();
        if (from.isDirectory()) {
            MediaScannerUtils.getPathsOfFolder(affectedPaths, from);
        } else {
            affectedPaths.add(from.getAbsolutePath());
        }
    }

    @Override
    protected void onResult(boolean success, RenameArguments args) {
        if (success) {
            File dest = args.getTarget();
           // BaseFilesFragment.refresh(context,args.getTarget().getParentFile());
            args.getBaseFragment().refresh(context,args.getTarget().getParentFile());
            MediaScannerUtils.informPathsDeleted(context, affectedPaths);
            if (dest.isFile()) {
                MediaScannerUtils.informFileAdded(context, dest);
            } else {
                MediaScannerUtils.informFolderAdded(context, dest);
            }
        } else {
            UIUtils.toast(context, context.getString(R.string.toast_error_on_rename));
        }
    }

    @Override
    protected void onAccessDenied() {
    }

    @Override
    protected void onRequestingAccess() {
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }
}
