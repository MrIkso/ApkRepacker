package com.mrikso.apkrepacker.ui.apkbuilder;

import com.google.common.collect.Lists;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.smali.SmaliOptions;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DexEncoder {

    public static void smali2Dex(String srcDirectory, String outputDexFile, ISmaliCallback callback) throws Exception {
        boolean errors;
        final DexBuilder dexBuilder;
        ExecutorService executor;
        final SmaliOptions options = new SmaliOptions();
        options.jobs = Runtime.getRuntime().availableProcessors();
        options.apiLevel = 15;
        options.outputDexFile = outputDexFile;
        options.allowOdexOpcodes = false;
        options.verboseErrors = false;
        System.currentTimeMillis();
        try {
            LinkedHashSet<File> filesToProcessSet = new LinkedHashSet<>();
            getSmaliFilesInDir(new File(srcDirectory), filesToProcessSet);
            errors = false;
            dexBuilder = new DexBuilder(Opcodes.forApi(15));
            executor = Executors.newFixedThreadPool(options.jobs);
            List<Future<Boolean>> tasks = Lists.newArrayList();
            for (File file : filesToProcessSet) {
                tasks.add(executor.submit(() -> assembleSmaliFile(file, dexBuilder, options)));
            }
            int totalTasks = tasks.size();
            int finishedTasks = 0;
            for (Future<Boolean> task : tasks) {
                int finishedTasks2 = finishedTasks;
                boolean errors2 = errors;
                while (true) {
                    try {
                        if (!task.get()) {
                            errors2 = true;
                        }
                        finishedTasks2++;
                        callback.updateAssembledFiles(finishedTasks2, totalTasks);
                        break;
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
                errors = errors2;
                finishedTasks = finishedTasks2;
            }
            executor.shutdown();
            if (!errors) {
                dexBuilder.writeTo(new FileDataStore(new File(options.outputDexFile)));
                System.currentTimeMillis();
            }
        } catch (Exception ex) {
            throw new Exception("Encountered errors while compiling smali files.");
        }
    }

    private static void getSmaliFilesInDir(File dir, Set<File> smaliFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getSmaliFilesInDir(file, smaliFiles);
                } else if (file.getName().endsWith(".smali")) {
                    smaliFiles.add(file);
                }
            }
        }
    }

    public static boolean assembleSmaliFile(File smaliFile, DexBuilder dexBuilder, SmaliOptions options) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(smaliFile);
            smaliFlexLexer lexer = new smaliFlexLexer(new InputStreamReader(fis, StandardCharsets.UTF_8), options.apiLevel);
            lexer.setSourceFile(smaliFile);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            smaliParser parser = new smaliParser(tokens);
            parser.setVerboseErrors(options.verboseErrors);
            parser.setAllowOdex(options.allowOdexOpcodes);
            parser.setApiLevel(options.apiLevel);
            smaliParser.smali_file_return result = parser.smali_file();
            /*if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
                String errorMsg = ((smaliFlexLexer) lexer).getErrorMessages();
                if (errorMsg.equals("")) {
                    throw new Exception("Error occurred while compiling " + smaliFile.getName());
                }
                throw new Exception(errorMsg);
            }*/
            CommonTree t = result.getTree();
            CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
            treeStream.setTokenStream(tokens);
            if (options.printTokens) {
                System.out.println(t.toStringTree());
            }
            smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
            dexGen.setApiLevel(options.apiLevel);
            dexGen.setVerboseErrors(options.verboseErrors);
            dexGen.setDexBuilder(dexBuilder);
            dexGen.smali_file();
            /*if (dexGen.getNumberOfSyntaxErrors() != 0) {
                String errorMsg2 = ((smaliFlexLexer) lexer).getErrorMessages();
                if (errorMsg2.equals("")) {
                    throw new Exception("Error occurred while compiling " + smaliFile.getName());
                }
                throw new Exception(errorMsg2);
            }*/
            fis.close();
            return true;
        } catch (Throwable th) {
            if (fis != null) {
                fis.close();
            }
            throw th;
        }
    }

}
