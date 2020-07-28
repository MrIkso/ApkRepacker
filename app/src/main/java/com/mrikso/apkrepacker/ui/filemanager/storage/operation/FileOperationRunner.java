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
import com.mrikso.apkrepacker.ui.filemanager.storage.access.StorageAccessManager;

import java.io.IOException;

/**
 * Manages a {@link FileOperation} instance's write access to different kinds of storage
 * devices and internally handles write access requests needed for the operation to succeed no
 * matter the location of the files being operated on.
 */
public class FileOperationRunner {
    private final StorageAccessManager storageAccessManager;
    private final Context context;

    FileOperationRunner(StorageAccessManager storageAccessManager, Context contenxt) {
        this.storageAccessManager = storageAccessManager;
       this.context = contenxt;
    }

    public <O extends FileOperation<A>, A extends FileOperation.Arguments> void run(O operation, A args) throws IOException {
        operation.onStartOperation(args);
        boolean success = operation.operate(args);
        boolean failedButNeedsAccess = !success && operation.needsWriteAccess();
        if (failedButNeedsAccess) {
            if (storageAccessManager.hasWriteAccess(args.getTarget())) {
                if (storageAccessManager.isSafBased()) {
                    success = operation.operateSaf(args);
                }
                operation.onResult(success, args);
            } else {
                operation.onRequestingAccess();
                storageAccessManager.requestWriteAccess(args.getTarget(), new StorageAccessManager.AccessPermissionListener() {
                    @Override
                    public void granted() {
                        try {
                            run(operation, args);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void denied() {
                        operation.onAccessDenied();
                    }

                    @Override
                    public void error() {
                        UIUtils.toast(context, context.getString(R.string.toast_error_grant_permisson_sd_card));
                       // toastDisplayer.grantAccessWrongDirectory();
                        try {
                            run(operation, args);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else {
            operation.onResult(success, args);
        }
    }
}
