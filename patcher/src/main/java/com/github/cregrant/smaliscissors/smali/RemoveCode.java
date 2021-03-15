package com.github.cregrant.smaliscissors.smali;

import com.github.cregrant.smaliscissors.Main;
import com.github.cregrant.smaliscissors.Prefs;
import com.github.cregrant.smaliscissors.structures.SmaliMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

class RemoveCode {
    private Stack<String> registers = new Stack<>();
    private Stack<Integer> registerStartPosition = new Stack<>();
    static boolean deleteInsteadofComment = false;

    public void cleanupMethod(SmaliMethod method, String target) {
        String body = method.getBody();
        String[] lines = body.split("\\R");
        scanMethodSignature(method, target);
        scanMethodBody(lines, target);
        deleteRegisters(method, lines);
        StringBuilder sb = new StringBuilder(200);
        for (String s : lines) {
            sb.append(s);
            if (Prefs.isWindows)
                sb.append("\r\n");
            else
                sb.append("\n");
        }
        String newBody = sb.toString();
        method.setBody(newBody);
    }

    private void deleteRegisters(SmaliMethod method, String[] lines) {
        if (method.getName().contains("updateTrackingData"))
            Main.out.println("");
        long time = System.currentTimeMillis();
        ArrayList<String> preventLoopsIf = new ArrayList<>();
        while (!registers.isEmpty()) {
            String register = registers.pop();
            int i = registerStartPosition.pop();
            ArrayList<Integer> preventLoopsGoto = new ArrayList<>();

            for (; i<lines.length; i++) {
                if (System.currentTimeMillis()-time>1000) {
                    Main.out.println("Too long operation: " + method.getPath() + " - " + register);
                }
                if (lines[i].length()<=4 || lines[i].startsWith("#"))
                    continue;
                String line = lines[i];
                String outputRegister = outputRegister(lines, i);
                boolean inputRegisterUsed = inputRegisterUsed(line, register);
                if (line.startsWith("    return")) {
                    if (inputRegisterUsed)
                        addHelpMark(method, lines, i);
                    break;
                }
                else if (line.startsWith("    goto")) {
                    if (!preventLoopsGoto.contains(i)) {
                        preventLoopsGoto.add(i);
                        i = searchTag(lines, i);
                    }
                    else
                        break;
                }
                else if (line.startsWith("    if")) {
                    if (inputRegisterUsed)
                        addHelpMark(method, lines, i);
                    if (!preventLoopsIf.contains(register+i)) {
                        preventLoopsIf.add(register+i);
                        registers.add(register);
                        registerStartPosition.add(searchTag(lines, i));
                    }
                }
                else if (inputRegisterUsed) {
                    deleteLine(lines, i);
                    if (outputRegister!=null && !outputRegister.equals(register)) {
                        registers.add(register);
                        registerStartPosition.add(i);
                    }
                }
                else if (outputRegister!=null && outputRegister.equals(register))
                    break;
            }
        }
    }


    private int searchTag(String[] lines, int i) {
        String line = lines[i];
        String tag;
        int start = line.lastIndexOf(':');
        int end = line.indexOf(' ', start);

        if (end!=-1)
            tag = "    " + line.substring(start, end);
        else
            tag = "    " + line.substring(start);

        int size = lines.length;
        for (int j=0; j<size; j++) {
            if (lines[j].startsWith(tag)) {
                return j;
            }
        }
        Main.out.println("Critical error: tag" + tag.replace("   ", "") + " not found.");
        System.exit(1);
        return 0;
    }

    private String outputRegister(String[] lines, int i) {
        String line = lines[i];
        if (line.startsWith("    invoke") && lines[i+2].startsWith("    move-result"))
            return lines[i+2].substring(lines[i+2].lastIndexOf(' ') + 1);
        else if (line.startsWith("    sub") || line.startsWith("    if") || line.startsWith("    sget") ||
                line.startsWith("    iget") || line.startsWith("    const") || line.startsWith("    new") ||
                (!line.startsWith("    move-result") && line.startsWith("    move")))
            return line.substring(line.indexOf(' ', 6) + 1, line.indexOf(','));
            //else if (line.startsWith("    return"))
            //    return line.substring(line.indexOf(' ', 6) + 1);
        else
            return null;
    }

    private void deleteLine(String[] lines, int i) {
        String line = lines[i];
        if (line.startsWith("    invoke")) {
            lines[i] = commentLine(lines[i]);
            if (lines[i+2].startsWith("    move-result")) {
                lines[i+2] = commentLine(lines[i+2]);
            }
        }
        else if (line.startsWith("    if")) {
            lines[i] = commentLine(lines[i]);
            int tagPos = searchTag(lines, i);
            lines[tagPos] = commentLine(lines[tagPos]);
        }
        else
            lines[i] = commentLine(lines[i]);
    }

    private String commentLine(String line) {
        if (deleteInsteadofComment)
            return "";
        else {
            if (line.startsWith("#"))
                return line;
            else
                return '#' + line;
        }
    }

    private boolean inputRegisterUsed(String line, String register) {
        if (!line.startsWith("    invoke") && !line.startsWith("    iput") &&
                !line.startsWith("    if") && !line.startsWith("    sput"))
            return false;

        if (line.contains("..")) {
            int registerNum = Integer.parseInt(register.substring(1));
            int from = Integer.parseInt(line.substring(line.indexOf("{", 8)+2, line.indexOf("..")-1));
            int to = Integer.parseInt(line.substring(line.indexOf("..")+4, line.indexOf("}")));
            char type = line.charAt(line.indexOf("{")+1);
            return register.charAt(0) == type && registerNum >= from && registerNum <= to;
        }
        int start;
        int end;
        if (line.contains("{")) {
            start = line.indexOf("{", 9)+1;
            end = line.lastIndexOf("}")+1;
        }
        else {
            start = line.indexOf(" ", 9);
            end = line.length();
        }
        char[] chars = line.substring(start, end).toCharArray();
        StringBuilder sb = new StringBuilder(3);
        for (char ch : chars) {
            if (ch=='v' || ch=='p') {
                sb = new StringBuilder(3);
                sb.append(ch);
            }
            else if (ch>=48 && ch<=57)
                sb.append(ch);
            else if (ch==',' || ch=='}')
                if (sb.toString().equals(register))
                    return true;
        }
        return false;
    }

    private void scanMethodSignature(SmaliMethod method, String target) {
        ArrayList<String> inputObjects = method.inputObjects;
        int size = inputObjects.size();
        ArrayList<String> inputObjectsCleaned = new ArrayList<>(size);

        for (int i=0; i<size; i++) {
            String obj = inputObjects.get(i);
            if (obj.contains(target)) {
                registerStartPosition.add(0);
                if (method.isStatic)
                    registers.add('p' + String.valueOf(i+1));
                else
                    registers.add('p' + String.valueOf(i));
            }
            else
                inputObjectsCleaned.add(obj);
        }
        method.inputObjectsCleaned = inputObjectsCleaned;
    }

    private void scanMethodBody(String[] lines, String target) {
        int size = lines.length;
        for (int i=0; i<size; i++) {
            if (lines[i].contains(target)) {
                String register = outputRegister(lines, i);
                deleteLine(lines, i);
                if (register != null) {
                    registers.add(register);
                    registerStartPosition.add(i);
                }
            }
        }
        registers.sort(Collections.reverseOrder());
        registerStartPosition.sort(Collections.reverseOrder());
    }

    static void addHelpMark(SmaliMethod method, String[] lines, int i) {
        if (!lines[i].contains("    #HELP")) {
            lines[i] = lines[i] + "    #HELP";
            Main.out.println(method.getPath() + " problem detected");
        }
    }
}

