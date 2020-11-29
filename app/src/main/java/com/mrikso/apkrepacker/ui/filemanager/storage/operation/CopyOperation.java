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

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.storage.DocumentFileUtils;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CopyArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.ui.OperationStatusDisplayer;
import com.mrikso.apkrepacker.ui.filemanager.utils.FileUtils;
import com.mrikso.apkrepacker.ui.filemanager.utils.MediaScannerUtils;
import com.mrikso.codeeditor.util.DLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class CopyOperation extends FileOperation<CopyArguments> {
    private final Context context;
    private final OperationStatusDisplayer statusDisplayer;

    public CopyOperation(Context context, OperationStatusDisplayer statusDisplayer) {
        this.context = context.getApplicationContext();
        this.statusDisplayer = statusDisplayer;
    }

    @Override
    public boolean operate(CopyArguments args) {
        return new NormalCopier(context, statusDisplayer, id).copy(args);
    }

    @Override
    public boolean operateSaf(CopyArguments args) {
        return new SafCopier(context, statusDisplayer, id).copy(args);
    }

    @Override
    public void onStartOperation(CopyArguments args) {
    }

    @Override
    public void onResult(boolean success, CopyArguments target) {
        if (success) {
            statusDisplayer.showCopySuccess(id, target.getTarget());
           // BaseFilesFragment.refresh(context,target.getTarget());
            //target.getBaseFragment().refresh(context, target.getTarget());
        } else {
            statusDisplayer.showCopyFailure(id, target.getTarget());
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

    private abstract static class Copier {
        private static final int COPY_BUFFER_SIZE = 32 * 1024;

        @NonNull
        private final Context context;
        @NonNull
        private final OperationStatusDisplayer statusDisplayer;
        private final int operationId;

        Copier(@NonNull Context context, @NonNull OperationStatusDisplayer statusDisplayer, int operationId) {
            this.context = context;
            this.statusDisplayer = statusDisplayer;
            this.operationId = operationId;
        }

        boolean copy(CopyArguments args) {
            List<FileHolder> files = args.getFilesToCopy();
            File destDirectory = args.getTarget();

            int fileCount = FileUtils.countFilesUnder(files);
            int filesCopied = 0;

            for (FileHolder origin : files) {
                File dest = FileUtils.createUniqueCopyName(context, destDirectory, origin.getName());
                if (dest != null) {
                    filesCopied = copyFileOrDirectory(
                            filesCopied, fileCount, origin.getFile(), dest);

                    if (origin.getFile().isDirectory()) {
                        MediaScannerUtils.informFolderAdded(context, dest);
                    } else {
                        MediaScannerUtils.informFileAdded(context, dest);
                    }
                }
            }

            return filesCopied == fileCount;
        }

        /**
         * Recursively copy a folder.
         *
         * @param filesCopied Initial value of how many files have been copied.
         * @param oldFile     Folder to copy.
         * @param newFile     The dir to be created.
         * @return The new filesCopied count.
         */
        private int copyFileOrDirectory(int filesCopied, int fileCount, @NonNull File oldFile, @NonNull File newFile) {
            if (oldFile.isDirectory()) {
                filesCopied = copyDirectory(filesCopied, fileCount, oldFile, newFile);
            } else {
                filesCopied = copyFile(filesCopied, fileCount, oldFile, newFile);
            }

            return filesCopied;
        }

        /**
         * Copy a file.
         *
         * @param filesCopied Initial value of how many files have been copied.
         * @param oldFile     File to copy.
         * @param newFile     The file to be created.
         * @return The new filesCopied count.
         */
        private int copyFile(int filesCopied, int fileCount, File oldFile, File newFile) {
            statusDisplayer.showCopyProgress(operationId, newFile.getParentFile(), oldFile,
                    filesCopied, fileCount);

            try (
                    FileInputStream input = new FileInputStream(oldFile);
                    OutputStream output = outputStream(newFile)
            ) {
                int len;
                byte[] buffer = new byte[COPY_BUFFER_SIZE];
                while ((len = input.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            } catch (IOException e) {
                com.mrikso.apkrepacker.utils.common.DLog.e(e);
                return filesCopied;
            }
            return filesCopied + 1;
        }

        private int copyDirectory(int filesCopied, int fileCount, @NonNull File oldFile,
                                  @NonNull File newFile) {
            if (!newFile.exists()) mkDir(newFile);

            // list all the directory contents
            String[] files = oldFile.list();

            assert files != null;
            for (String file : files) {
                // construct the src and dest file structure
                File srcFile = new File(oldFile, file);
                File destFile = new File(newFile, file);
                // recursive copy
                filesCopied = copyFileOrDirectory(filesCopied, fileCount, srcFile, destFile);
            }
            return filesCopied;
        }

        @NonNull
        protected abstract OutputStream outputStream(File newFile) throws FileNotFoundException;

        @SuppressWarnings("UnusedReturnValue")
        protected abstract boolean mkDir(@NonNull File newFile);
    }

    private static class NormalCopier extends Copier {
        NormalCopier(@NonNull Context context,
                     @NonNull OperationStatusDisplayer statusDisplayer,
                     int operationId) {
            super(context, statusDisplayer, operationId);
        }

        @Override
        @NonNull
        protected OutputStream outputStream(File newFile) throws FileNotFoundException {
            return new FileOutputStream(newFile);
        }

        @Override
        protected boolean mkDir(@NonNull File newFile) {
            return newFile.mkdir();
        }
    }

    private static class SafCopier extends Copier {
        private final Context context;

        SafCopier(@NonNull Context context,
                  @NonNull OperationStatusDisplayer statusDisplayer,
                  int operationId) {
            super(context, statusDisplayer, operationId);
            this.context = context;
        }

        @NonNull
        @Override
        protected OutputStream outputStream(File newFile) throws FileNotFoundException {
            boolean fileCreated = false;
            try {
                // If target is accessible without SAF (in case of cross-media moves)
                fileCreated = newFile.createNewFile();
            } catch (IOException e) {
                DLog.log(e);
            }

            if (fileCreated) {
                return new FileOutputStream(newFile);
            } else {
                DocumentFile toSaf = DocumentFileUtils.createFile(context, newFile, "*/*");
                if (toSaf == null) throw new FileNotFoundException();
                return DocumentFileUtils.outputStreamFor(toSaf, context);
            }
        }

        @Override
        protected boolean mkDir(@NonNull File newDir) {
            return DocumentFileUtils.createDirectory(context, newDir) != null;
        }
    }
}
