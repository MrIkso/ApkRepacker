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

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.storage.DocumentFileUtils;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CreateDirectoryArguments;

import java.io.File;

public class CreateDirectoryOperation extends FileOperation<CreateDirectoryArguments> {
    private final Context context;


    public CreateDirectoryOperation(Context context) {
        this.context = context;
    }

    @Override
    public boolean operate(CreateDirectoryArguments args) {
        File dest = args.getTarget();

        return dest.exists() || dest.mkdirs();
    }

    @Override
    public boolean operateSaf(CreateDirectoryArguments args) {
        File dest = args.getTarget();

        return dest.exists() || DocumentFileUtils.createDirectory(context, dest) != null;
    }

    @Override
    public void onStartOperation(CreateDirectoryArguments args) {
    }

    @Override
    public void onResult(boolean success, CreateDirectoryArguments args) {
        if (success) {
            UIUtils.toast(context, context.getString(R.string.directory_created_sucess));
            args.getBaseFragment().refresh(context,args.getTarget().getParentFile());
        } else {
            UIUtils.toast(context, context.getString(R.string.error));
        }
    }

    @Override
    public void onAccessDenied() {
    }

    @Override
    public void onRequestingAccess() {
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }
}
