/**
 * Copyright (C) 2019 Ryszard Wiśniewski <brut.alll@gmail.com>
 * Copyright (C) 2019 Connor Tumbleson <connor.tumbleson@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mrikso.apkrepacker.ui.apkbuilder;

import com.google.common.collect.Lists;

import org.antlr.runtime.RecognitionException;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.smali.SmaliOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import brut.androlib.AndrolibException;
import brut.androlib.mod.SmaliMod;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
public class SmaliBuilder {
    private final static Logger LOGGER = Logger.getLogger(SmaliBuilder.class.getName());
    private final ExtFile mSmaliDir;
    private final File mDexFile;
    private int mApiLevel = 0;
    private ISmaliCallback mSmaliCallback;

    public SmaliBuilder(ExtFile smaliDir, File dexFile, int apiLevel) {
        mSmaliDir = smaliDir;
        mDexFile = dexFile;
        mApiLevel = apiLevel;
    }

    public static void build(ExtFile smaliDir, File dexFile, int apiLevel) throws AndrolibException {
        new SmaliBuilder(smaliDir, dexFile, apiLevel).build();
    }

    public static void build(ExtFile smaliDir, File dexFile) throws AndrolibException {
        new SmaliBuilder(smaliDir, dexFile, 0).build();
    }

    public void setCallback(ISmaliCallback callback){
        mSmaliCallback = callback;
    }

    public void build() throws AndrolibException {
        final SmaliOptions options = new SmaliOptions();
        options.jobs = Runtime.getRuntime().availableProcessors();
        options.apiLevel = mApiLevel;
        options.allowOdexOpcodes = false;
        options.verboseErrors = false;
        ExecutorService executor;
        try {
            boolean errors = false;
            DexBuilder dexBuilder;
            if (mApiLevel > 0) {
                dexBuilder = new DexBuilder(Opcodes.forApi(mApiLevel));
            } else {
                dexBuilder = new DexBuilder(Opcodes.getDefault());
            }
            executor = Executors.newFixedThreadPool(options.jobs);

            LinkedHashSet<String> smaliFiles = new LinkedHashSet<>(mSmaliDir.getDirectory().getFiles(true));

            List<Future<Boolean>> tasks = Lists.newArrayList();
            for (String fileName : smaliFiles) {

                    tasks.add((Future<Boolean>) executor.submit(() -> {
                        try {
                            buildFile(fileName, dexBuilder);
                        } catch (AndrolibException| IOException e) {
                            e.printStackTrace();
                        }
                    }));

            }
            int totalTasks = tasks.size();
            int finishedTasks = 0;
            for (Future<Boolean> task : tasks) {
                int finishedTasks2 = finishedTasks;
               boolean errors2 = errors;
                while (true) {
                    try {
                        task.get();
                        finishedTasks2++;
                        mSmaliCallback.updateAssembledFiles(finishedTasks2, totalTasks);
                        break;
                    } catch (InterruptedException|ExecutionException e) {
                        e.printStackTrace();
                        errors2 = true;
                    }
                }
               errors = errors2;
                finishedTasks = finishedTasks2;
            }
            executor.shutdown();

          //  mSmaliCallback.updateAssembledFiles();
           /* for (String fileName : smaliFiles) {
                buildFile(fileName, dexBuilder);
            }*/

            //finally build dex file
            dexBuilder.writeTo(new FileDataStore(new File(mDexFile.getAbsolutePath())));
        } catch (IOException | DirectoryException ex) {
            throw new AndrolibException(ex);
        }
    }

    private void buildFile(String fileName, DexBuilder dexBuilder)
            throws AndrolibException, IOException {
        File inFile = new File(mSmaliDir, fileName);
        InputStream inStream = new FileInputStream(inFile);

        if (fileName.endsWith(".smali")) {
            try {
                if (!SmaliMod.assembleSmaliFile(inFile, dexBuilder, mApiLevel, false, false)) {
                    throw new AndrolibException("Could not smali file: " + fileName);
                }
            } catch (IOException | RecognitionException ex) {
                throw new AndrolibException(ex);
            }
        } else {
            LOGGER.warning("Unknown file type, ignoring: " + inFile);
        }
        inStream.close();
    }
}
