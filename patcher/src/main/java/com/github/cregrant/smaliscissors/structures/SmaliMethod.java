package com.github.cregrant.smaliscissors.structures;

import com.github.cregrant.smaliscissors.Gzip;

import java.util.ArrayList;

public class SmaliMethod {
    private final String path;
    private final String name;
    private final String modifiers;
    private Gzip body;
    public boolean isStatic;
    private final SmaliClass parentClass;
    public ArrayList<String> inputObjects = new ArrayList<>(4);
    public ArrayList<String> inputObjectsCleaned = new ArrayList<>(4);
    private final String outputObject;

    public SmaliMethod(SmaliClass smaliClass, String signature, String methodBody) {
        int inputStart = signature.indexOf('(');
        int inputEnd = signature.indexOf(')');
        int nameBegin = signature.lastIndexOf(' ')+1;
        modifiers = signature.substring(0, nameBegin);
        isStatic = signature.contains("static");
        name = signature.substring(nameBegin, inputStart);
        path = smaliClass.getPath() + ";->" + name;
        outputObject = signature.substring(inputEnd+1);
        if (inputEnd-inputStart<=1)
            inputObjects.add("");
        else
            parseInputObjects(signature.substring(inputStart+1, inputEnd));

        parentClass = smaliClass;
        body = new Gzip(methodBody);
    }

    private void parseInputObjects(String input) {
        int start = 0;
        char[] chars = input.toCharArray();
        char prevChar = '(';
        for (int i=0; i<chars.length; i++) {
            char currentChar = chars[i];
            switch (currentChar) {
                case '[':
                    if (prevChar!='[') {
                        start = i;
                    }
                    break;

                case 'I':
                case 'B':
                case 'Z':
                case 'V':
                case 'S':
                case 'C':
                case 'D':
                case 'J':
                case 'F':
                    if (prevChar!='/' && (prevChar=='[' || start==i)) {
                        if (prevChar=='[')
                            inputObjects.add(input.substring(start, i));
                        else
                            inputObjects.add(String.valueOf(currentChar));
                        start = i+1;
                    }
                    break;

                case ';':
                    inputObjects.add(input.substring(start, i+1));
                    start = i+1;
                    break;
            }
            prevChar = currentChar;
        }
    }

    public String getOldSignature() {
        return buildSignature(inputObjects);
    }

    public String getNewSignature() {
        return buildSignature(inputObjectsCleaned);
    }

    private String buildSignature(ArrayList<String> input) {
        if (input.isEmpty())
            return modifiers + name + "()" + outputObject;

        StringBuilder sb = new StringBuilder();
        for (String obj : input) {
            sb.append(obj);
        }
        return modifiers + name + '('+sb.toString()+')' + outputObject;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body.decompress();
    }

    public void setBody(String newBody) {
        this.body = new Gzip(newBody);
    }

    public SmaliClass getParentClass() {
        return parentClass;
    }
}
