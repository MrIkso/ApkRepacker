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

import com.mrikso.apkrepacker.fragment.base.BaseFilesFragment;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.storage.DocumentFileUtils;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CopyArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.MoveArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.ui.OperationStatusDisplayer;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.ui.OperationStatusDisplayerInjector;
import com.mrikso.apkrepacker.ui.filemanager.utils.FileUtils;
import com.mrikso.apkrepacker.ui.filemanager.utils.MediaScannerUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MoveOperation extends FileOperation<MoveArguments> {
    private final Context context;
    private final OperationStatusDisplayer statusDisplayer;

    public MoveOperation(Context context, OperationStatusDisplayer statusDisplayer) {
        this.context = context.getApplicationContext();
        this.statusDisplayer = statusDisplayer;
    }

    @Override
    public boolean operate(MoveArguments args) {
        return new NormalMover().move(args);
    }

    @Override
    public boolean operateSaf(MoveArguments args) {
        return new SafMover().move(args);
    }

    @Override
    public void onStartOperation(MoveArguments args) {
    }

    @Override
    public void onResult(boolean success, MoveArguments args) {
        if (success) {
            statusDisplayer.showMoveSuccess(id, args.getTarget());
          //  BaseFilesFragment.refresh(context,args.getTarget().getParentFile());
          //  args.getBaseFragment().refresh(context,args.getTarget().getParentFile());
        } else {
            statusDisplayer.showMoveFailure(id, args.getTarget());
        }
    }

    @Override
    public void onAccessDenied() {
    }

    @Override
    public void onRequestingAccess() {
       // clearNotification(id, context);
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }

    private abstract class Mover {
        boolean move(MoveArguments args) {
            boolean allSucceeded = true;
            int fileIndex = 0;

            File from;
            File toFile;
            File target = args.getTarget();
            List<FileHolder> files = args.getFilesToMove();
            for (FileHolder fh : files) {
                statusDisplayer.showMoveProgress(id, target, fh.getFile(),
                        fileIndex++, files.size());

                from = fh.getFile().getAbsoluteFile();
                toFile = new File(target, fh.getName());
                List<String> paths = FileUtils.getPathsUnder(from);

                boolean fileMoved = moveSingle(fh, toFile,args.getBaseFragment());

                if (fileMoved) {
                    MediaScannerUtils.informPathsDeleted(context, paths);
                    if (toFile.isDirectory()) {
                        MediaScannerUtils.informFolderAdded(context, toFile);
                    } else {
                        MediaScannerUtils.informFileAdded(context, toFile);
                    }
                }

                allSucceeded &= fileMoved;
            }

            return allSucceeded;
        }

        protected abstract boolean moveSingle(FileHolder what, File futureWhat, BaseFilesFragment filesFragment);
    }

    private class NormalMover extends Mover {
        @Override
        protected boolean moveSingle(FileHolder what, File futureWhat, BaseFilesFragment filesFragment) {
            return what.getFile().renameTo(futureWhat);
        }
    }

    private class SafMover extends Mover {
        @Override
        protected boolean moveSingle(FileHolder what, File futureWhat, BaseFilesFragment filesFragment) {
            boolean copySucceeded = new CopyOperation(context, OperationStatusDisplayerInjector.noOpStatusDisplayer())
                    .operateSaf(CopyArguments.copyArgs(Collections.singletonList(what), futureWhat.getParentFile(), filesFragment));
            // Only delete if full tree was copied. Spare files are bad, disappearing files are worse
            return copySucceeded && DocumentFileUtils.safAwareDelete(context, what.getFile());
        }
    }
}
