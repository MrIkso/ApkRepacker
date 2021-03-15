package com.github.cregrant.smaliscissors.smali;

import com.github.cregrant.smaliscissors.BackgroundWorker;
import com.github.cregrant.smaliscissors.Main;
import com.github.cregrant.smaliscissors.Prefs;
import com.github.cregrant.smaliscissors.structures.*;

import java.util.ArrayList;
import java.util.Collections;

public class SmaliAnalyzer {

    public void analyze(ArrayList<DecompiledFile> rawSmaliFiles, Rule rule) {
        if (rule.targetArr==null)
            rule.targetArr = new ArrayList<>(Collections.singleton(rule.target));

        for (String target : rule.targetArr) {
            if (target.endsWith("*"))
                target = target.replace("*", "");       // * is useless here
            else if (target.endsWith(".smali"))
                target = target.replace(".smali", ";"); //target is a single file
            ArrayList<SmaliClass> classes = separateSmali(rawSmaliFiles, target);
            ArrayList<SmaliField> fields = extractFields(classes, target);
            ArrayList<SmaliMethod> methods = extractMethods(classes, target);
            ArrayList<SmaliMethod> cleanedMethods = new ArrayList<>(methods.size());

            BackgroundWorker.createIfTerminated();
            Main.out.println("Begin cleaning " + methods.size() + " methods.");

            for (SmaliMethod method : methods) {
                String finalTarget = target;
                //Runnable r = () -> {
                    new RemoveCode().cleanupMethod(method, finalTarget);
                    //todo add error handling
                    cleanedMethods.add(method);
                //};
                //BackgroundWorker.executor.submit(r);
            }
            BackgroundWorker.computeAndDestroy();
            System.out.println("HONK");
            SmaliClass smaliClass;
            String body;
            int count = 0;
            for (SmaliMethod method : cleanedMethods) {
                smaliClass = method.getParentClass();
                body = smaliClass.getFile().getBody();
                int startIndex = body.indexOf(method.getOldSignature());
                if (startIndex==-1) {
                    Main.out.println("Error: old method not found!");
                    continue;
                }
                count++;
                int endIndex = body.indexOf(".end method", startIndex)+12;
                String newBody = body.substring(0, startIndex) + method.getNewSignature() + '\n' + method.getBody() + body.substring(endIndex);
                if (body.equals(newBody) && RemoveCode.deleteInsteadofComment)
                    Main.out.println("Error: nothing changed in " + smaliClass.getPath());
                smaliClass.getFile().setBody(newBody);
            }
            Main.out.println(count + " methods cleaned successfully.");
        }
    }

    private ArrayList<SmaliClass> separateSmali(ArrayList<DecompiledFile> rawSmaliFiles, String target) {
        ArrayList<SmaliClass> classes = new ArrayList<>(20);
        BackgroundWorker.createIfTerminated();
        for (DecompiledFile df : rawSmaliFiles) {
            if (!df.getPath().contains(target)) {
                //Runnable r = () -> {
                    if (df.getBody().contains(target)) {
                        SmaliClass smaliClass = new SmaliClass(df, target);
                        classes.add(smaliClass);
                    }
                //};
                //BackgroundWorker.executor.submit(r);
            }
        }
        BackgroundWorker.computeAndDestroy();
        return classes;
    }

    ArrayList<SmaliField> extractFields(ArrayList<SmaliClass> classes, String target) {
        ArrayList<SmaliField> fields = new ArrayList<>();
        for (SmaliClass smaliClass : classes) {
            String body = smaliClass.getFile().getBody();
            String[] lines;
            int s = body.indexOf(".method");
            if (s==-1)
                lines = body.split("\\R");
            else
                lines = body.substring(0, s).split("\\R");

            for (String line : lines) {
                if (line.startsWith(".field") && line.contains(target)) {
                    String name = line.substring(line.lastIndexOf(" ")+1);
                    SmaliField field = new SmaliField(smaliClass, name);
                    fields.add(field);
                }
            }
        }
        return fields;          //catch all fields that stores some targets.
    }

    ArrayList<SmaliMethod> extractMethods(ArrayList<SmaliClass> classes, String target) {
        ArrayList<SmaliMethod> methods = new ArrayList<>();
        for (SmaliClass smaliClass : classes) {
            String classBody = smaliClass.getFile().getBody();
            int s = classBody.indexOf(".method");
            if (s==-1)
                continue;
            String[] lines = classBody.substring(s).split("\\R");
            int size = lines.length;
            for (int i=0; i<size;) {
                if (lines[i].startsWith(".method")) {
                    String line = lines[i];
                    String signature = line;
                    StringBuilder bodyBuilder = new StringBuilder();
                    i++;
                    while (!lines[i].contains(".end method")) {
                        bodyBuilder.append(lines[i]);
                        if (Prefs.isWindows)
                            bodyBuilder.append("\r\n");
                        else
                            bodyBuilder.append("\n");
                        i++;
                    }
                    bodyBuilder.append(".end method");

                    String methodBody = bodyBuilder.toString();
                    if (signature.contains(target) || methodBody.contains(target)) {
                        SmaliMethod method = new SmaliMethod(smaliClass, signature, methodBody);
                        methods.add(method);
                    }
                }
                else
                    i++;
            }
        }
        return methods;          //catch all methods that stores some targets.
    }
}
