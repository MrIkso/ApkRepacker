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

package com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument;


import androidx.annotation.NonNull;

import com.mrikso.apkrepacker.fragment.base.BaseFilesFragment;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.FileOperation;

import java.io.File;

public class CreateFileArguments extends FileOperation.Arguments {
    private CreateFileArguments(@NonNull File newFile) {
        super(newFile);
    }

    public static CreateFileArguments createFileArguments(@NonNull File newFile) {
        return new CreateFileArguments(newFile);
    }
}