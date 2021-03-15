package com.github.cregrant.smaliscissors.structures;

import java.util.ArrayList;

public class SmaliField {
    private final String path;
    private final String name;
    private final SmaliClass parentClass;
    private ArrayList<String> links = new ArrayList<>(5);

    public SmaliField(SmaliClass smaliClass, String fieldName) {
        parentClass = smaliClass;
        name = fieldName;
        path = smaliClass.getPath()+";->"+fieldName;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public SmaliClass getParentClass() {
        return parentClass;
    }

    public void addLink(String link) {
        links.add(link);
    }

    public void removeLink(String link) {
        links.remove(link);
    }
}
